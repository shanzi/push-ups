(ns push-ups.forms
  (:use hiccup.form)) 

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
   (label "start-time" "Remind me at:")
   (drop-down "start-time"
              [["Forenoon" (map #(vector (format "%d am." %) %) (range 7 12))]
               ["Afternoon" (map #(vector (format "%d pm." (- % 12)) %) (range 13 19))]
               ["Night" (map #(vector (format "%d pm." (- % 12)) %) (range 19 23))]]
              time-choice)]))

(defn- test-input-row
  [label-text value checked seperators]
  (into [:tr]
        (let [radio-name (str "age" value)]
          [[:td (radio-button "age" checked radio-name)]
           [:td (label (str "age-" radio-name) label-text)]
           [:td (drop-down (str "result" value)
                           (into [] (for [[a b] (partition 2 1 seperators)
                                          :let [nstr (if (nil? b)
                                                       (format "%d & above" a)
                                                       (format "%d - %d" a b))]]
                                      [nstr nstr])))]])))

(def ^:private test-course 
  [["< 40" [0 6 15 30 50 150 nil]]
   ["40 - 55" [0 6 13 25 45 75 125 nil]]
   ["55 & above" [0 6 11 20 35 65 100 nil]]])


(defn exercise-time-choice-set
  ([legend]
   (exercise-time-choice-set legend "next-week" "1-3-5" 20))
  ([legend week-choice day-choice time-choice]
   [:fieldset.time-choice-set [:legend legend]
    (start-week week-choice)
    (day-in-week day-choice)
    (time-in-day time-choice)]))

(defn test-result-set
  ([legend]
   (test-result-set legend 0))
  ([legend checked-idx]
   [:fieldset.result-set [:legend legend] 
    (into [:table [:tr [:th ""] [:th "Your Age"] [:th {:width "60%"} "How many push-ups did you do?"]]]
          (map-indexed (fn [idx [label sep]] 
                         (test-input-row label idx (= idx checked-idx) sep))
                       test-course))]))

(defn initial-form
  "generate initial form for creating a new calendar"
  [action]
  (form-to [:post action]
           (exercise-time-choice-set "Arrange exercise time")
           (test-result-set "Input initial test result")
           [:p.form-controllers 
            (submit-button "Submit")
            (reset-button "Reset")]))
