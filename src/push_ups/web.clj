(ns push-ups.web
  (:use hiccup.page
        hiccup.element
        [ring.util.response :only (redirect response content-type)]
        [clj-time.core :only (day-of-week )]
        [clj-time.format :only (formatter-local unparse)])
  (:require [push-ups.forms :as forms]
            [push-ups.db :as db]
            [push-ups.plan :as plan]
            [push-ups.ical :as ical]
            clojure.pprint))


(def ^:private date-time-formatter 
 (formatter-local "yyyy-MM-dd, h a"))

(defn- to-string
  [datetime]
  (unparse date-time-formatter datetime))

(defn base
  "base html frameworks"
  [title & content]
  (html5
    (include-css "/statics/base.css")
    [:head [:title title]]
    [:body
     [:div#frame
      (image "/statics/logo.png" "logo")
      [:div.content content]
      [:div.footer "xiuxiu.de (c) 2013"]]
     (include-js "/statics/base.js")]))


(defn index []
  "index template"
  (base "push-ups plan"
        [:h1 "Push-ups plan"]
        [:p "This website is designed to help create calendar events 
            for your push-up exercise according to the instructions from "
         (link-to "http://www.hundredpushups.com/" "handredspushups.com") "."]
        [:p "No need to register, just fill in the form below, you will get an ics format
            calendar subscription source."]
        [:p.initial-form
         (forms/initial-form "/new")]))


(defn new-plan
  "New exercise endpoint"
  ([]
   (new-plan nil nil))
  ([params flash]
   (if (empty? params)
     (base "Create Exercise Plan"
           [:h1 "Create a new exercise plan"]
           (when-let [error (:error flash)] [:p.error error])
           (when-let [info (:info flash)] [:p.info info])
           (forms/initial-form ""))
     (let [start-date (forms/parse-start-date-time params)
           test-result (forms/parse-test-result params)]
       (if (nil? start-date)
         (new-plan nil {:error "There are errors in your form, please correct them"})
         (let [permalink (db/gen-permalink)]
           (if (db/new-ics-record permalink test-result start-date)
             (merge 
               (redirect (format "/i/%s" permalink))
               {:flash {:info "Your exercise plan has been successfully created."}})
             (new-plan nil {:error "Something is wrong, please try again"}))))))))

(defn- day-of-week-td
  [date]
  (let [sym (nth ["Sn" \M \T \W "Th" \F \S] (day-of-week date))]
    [(keyword (format "td.%s" sym)) sym]))

(defn- view-part
  [idx part colcount]
  (conj 
    [:table 
     [:tr [:th.title {:colspan (+ colcount 3)}
           (str "PART " (inc idx))]]]
    (conj [:tr [:th] [:th "Datetime"]]
          (for [i (range colcount)] [:th.set (str "Set " (inc i))])
          [:th "Rest*"]) 
    (map (fn [plan]
           (let [sets (:sets plan)
                 end  (:end plan)
                 date (:date plan)
                 rest_ (:rest plan)
                 ]
             (conj 
               (into [:tr (day-of-week-td date) [:td (to-string date)]]
                     (for [idx (range (dec colcount))] 
                       (if (< idx (count sets))
                         [:td (nth sets idx)]
                         [:td])))
               [:td.end (format "(> %d)" end)]
               [:td.rest rest_])))
         part)))

(defn- max-cols
  [parts]
  (inc (apply max (into [0] 
                        (map (fn [part]
                               (apply max (into [0]
                                                (map #(count (:sets %)) part)))) parts)))))


(defn view-plan
  "View plan infomation"
  [permalink flash]
  (when-let [ics-record (db/get-ics-record permalink)] 
    (base "View your exercise plan"
          (when-let [error (:error flash)] [:p.error error])
          (when-let [info (:info flash)] [:p.info info])
          [:h1 "Your plan detail"]
          [:p [:b "Rest*"] 
           " is the time of rest your should have between
           each set of push-ups, in seconds."]
          [:p "In the "
           [:b "last set"]
           ", you should execude as "
           [:b "many"]
           " push-ups as you can,
           and at least above the number given in the forms."]
          (let [parts (plan/plan-with-ics-record ics-record)
                colcount (max-cols parts)]
            (map-indexed #(view-part %1 %2 colcount) parts))
          (forms/test-form-for-ics-record ics-record))))

(defn record-test-result
  [permalink params]
  (when-let [ics-record (db/get-ics-record permalink)]
    (if (:final_test_r ics-record)
      (view-plan permalink {:info "This plan has already been finished."})
      (if-let [result (forms/parse-test-result params)]
        (if (:part_3_date ics-record)
          (do
            (db/update-ics-record permalink {:final_test_r result})
            (view-plan permalink {:info "Congratulations! You have finished your exercise plan."}))
          (when-let [datetime (forms/parse-start-date-time params)]
            (if-let [ret (if (or (>= (:part_1_test_r ics-record) 3) (:part_2_date ics-record))
                           (db/update-ics-record permalink {:part_3_test_r result 
                                                            :part_3_date datetime})
                           (db/update-ics-record permalink {:part_2_test_r result 
                                                            :part_2_date datetime}))]
              (view-plan permalink {:info  "New exercise plan generated!"})
              (view-plan permalink {:error  "Failed to generate new plan."}))))
        (view-plan permalink {:error "Test result should be a number!"})))))

(defn calendar
  [permalink]
  (when-let [ics-record (db/get-ics-record permalink)]
    (println (str "GET:" permalink))
    (content-type 
      (response (ical/ical-with-ics-record ics-record))
      "text/calendar")))
