(ns push-ups.forms
  (:use hiccup.form
        hiccup.element
        [clj-time.core :only (minus plus days day-of-week time-zone-for-offset from-time-zone today-at)])
  (:require clojure.pprint)) 


(defn- start-week
  "dropdown for selecting start date"
  ([] (start-week "next-week"))
  ([choice]
   [:p
    (label "start-date" "Start from:")
    (drop-down "start-date"
               [["This week" "this-week"]
                ["Next week" "next-week"] ]
               choice)]
   ))

(defn- day-in-week
  "dropdown for selecting day in week to do push-ups"
  ([] (day-in-week "1-3-5"))
  ([choice]
  [:p
   (label "day-in-week" "Do push-ups at:")
   (drop-down "day-in-week" 
              [["Mon., Wed., Fri." "1-3-5"]
               ["Tue., Thu., Sat." "2-4-6"]]
              choice)]))

(defn- time-in-day
  "dropdown for selecting time in a day to do push-ups"
  ([] (time-in-day 20))
  ([time-choice]
  [:p
   (label "time-in-day" "Remind me at:")
   (drop-down "time-in-day"
              [["Forenoon" (map #(vector (format "%d am." %) %) (range 7 12))]
               ["Afternoon" (map #(vector (format "%d pm." (- % 12)) %) (range 13 19))]
               ["Night" (map #(vector (format "%d pm." (- % 12)) %) (range 19 23))]]
              time-choice)]))

(defn exercise-time-choice-set
  ([legend]
   (exercise-time-choice-set legend nil))
  ([legend description]
   (exercise-time-choice-set legend "next-week" "1-3-5" 20 description))
  ([legend week-choice day-choice time-choice description]
   [:fieldset.time-choice-set [:legend legend]
    (when-not (or (nil? description) (false? description))
      (into [:p]description))
    (start-week week-choice)
    (day-in-week day-choice)
    (time-in-day time-choice)]))

(def test-course 
 [[0 6 11 21 26 nil]
  [16 21 25 nil]
  [31 36 40 nil]])

(defn test-result-set
  ([legend course]
   (test-result-set legend course nil))
  ([legend course description]
   (test-result-set legend  course 0 description))
  ([legend course checked-idx description]
   [:fieldset.result-set [:legend legend] 
    (when-not (or (nil? description) (false? description)) 
      (into [:p] description))
      [:p (label "result" "How many push-ups did you execude? ")]
      [:p (drop-down (str "result")
                  (into [] (map-indexed
                             (fn [idx [a b]]
                               [(if (nil? b)
                                  (format "%d & above" a)
                                  (format "%d - %d" a (dec b)))
                                idx])
                             (partition 2 1 course))))]]))

(defmacro form-with-timezone
  "generate form which will automatically add a timezone field and with submit controllers"
  [params & extra]
  `(form-to ~params 
            (hidden-field {:class "timezone-field"} "timezone")
            ~@extra))

(defn initial-form
  "generate initial form for creating a new calendar"
  [action]
  (form-with-timezone [:post action]
           (exercise-time-choice-set "Arrange exercise time"
                                     "Chose the date and time you'd like to arrange your push-ups exercise at.
                                     You will be able to change the settings after taken the first periodic test 
                                     two weeks later.")
           (test-result-set "Input initial test result"
                            (get test-course 0)
                            [nil 
                             "Before your begin your exercise, try to execute as many good-form push ups as you can
                            and count the number. Please click "
                            (link-to "http://www.hundredpushups.com/test.html" "here")
                            " for more infomation."])
           [:p.form-controllers 
            (submit-button "Submit")
            (reset-button "Reset")]))


(defn- day-of-this-week
  "return date of this monday"
  [now day]
  (minus now (days (- (day-of-week now) day))))

(defn- day-of-next-week
  "return date of next monday"
  [now day]
  (plus now (days (- (+ 7 day) (day-of-week now)))))

(defn parse-start-date-time
  "parse start-date option to date"
  [params]
  (let [timezone-offset (/ (read-string (:timezone params)) 60)
        week-option (:start-date params)
        day-option (if (= (:day-in-week params) "2-4-6") 2 1)
        time-option (:time-in-day params)
        now (from-time-zone (today-at (read-string time-option) 00) 
                            (time-zone-for-offset timezone-offset))]
    (case week-option
      "this-week" (day-of-this-week now day-option)
      "next-week" (day-of-next-week now day-option)
      nil)))

(defn parse-test-result
  [params]
  (let [result (:result params)]
    (when (re-matches #"\d+" result)
      (read-string (re-find #"[1-9]\d+$|0$" result)))))


    (defn periodic-form
      "generate initial form for creating a new calendar"
      [action part]
      [:div.periodic-test
       [:h4 "After this part's test, you should have a test to decide the plan of next part."]
       (form-with-timezone [:post action]
                           (exercise-time-choice-set "Rearrange exercise time"
                                                     "You can now rearrange your exercise time.")
                           (test-result-set "Input your test result this part"
                                            (nth test-course 
                                                 (case part
                                                   :part_2 1
                                                   :part_3 2)))
                           [:p.form-controllers 
                            (submit-button "Submit") (reset-button "Reset")])])

    (defn final-test-form
      [action]
      (form-with-timezone [:post action]
                          [:div.final-test
                           [:h3 "After finshed all your exercise plan, now lets take a final test!"]
                           [:p (label "result" "How many push-ups can you do now?")]
                           (text-field "result")]
                          [:p.form-controllers 
                           (submit-button "Submit") (reset-button "Reset")]))


    (defn test-form-for-ics-record
      [ics-record]
      (if-let [final-test-result (:final_test_r ics-record)]
        [:div.finished
         [:h3 "This plan has been finished."]
         [:div.final
          [:div "Final Test Result"]
          [:div.result final-test-result]
          [:div "push-ups"]]]
        (if-let [part-3-date (:part_3_date ics-record)] 
          (final-test-form "")
          (let [part-1-result (:part_1_test_r ics-record)
                part-2-date (:part_2_date ics-record)]
            (when part-1-result
              (if (or (>= part-1-result 3) part-2-date)
                (periodic-form "" :part_3)
                (periodic-form "" :part_2)))))))
