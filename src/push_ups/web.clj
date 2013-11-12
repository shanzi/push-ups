(ns push-ups.web
  (:use hiccup.page
        hiccup.element
        [ring.util.response :only (redirect not-found)])
  (:require [push-ups.forms :as forms]
            [push-ups.db :as db]
            [push-ups.plan :as plan]
            clojure.pprint))


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
   (clojure.pprint/pprint params)
   (if (empty? params)
     (base "Create Exercise Plan"
           [:h1 "Create a new exercise plan"]
           (when-not (nil? flash)
             [:p.error flash])
           (forms/initial-form ""))
     (let [start-date (forms/parse-start-date-time params)
           test-result (forms/parse-test-result params)]
       (if (nil? start-date)
         (new-plan nil "There are errors in your form, please correct them")
         (let [permalink (db/gen-permalink)]
          (if (db/new-ics-record permalink test-result start-date)
           (merge 
             (redirect (format "/i/%s" permalink))
             {:flash "Your exercise plan has been successfully created."})
           (new-plan nil "Something is wrong, please try again"))))))))


(defn- view-part
  [idx part colcount]
  (into (conj 
          [:table [:tr [:th {:colspan colcount} (str "PART " (inc idx))]]]
          (into [:tr [:th "Datetime"]] (for [i (range colcount)] [:th (str "Set " (inc i))]))) 
        (map (fn [plan]
               (let [sets (:sets plan)
                     end  (:end plan)
                     date (:date plan)]
                 (conj 
                   (into [:tr [:td (str date)]]
                         (map #(vector :td %) sets))
                   [:td (format "max (at least %d)" end)])))
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
  (let [ics-record (db/get-ics-record permalink)]
    (if (not ics-record)
      (not-found "Not Found")
      (base "View your exercise plan"
            (when-not (nil? flash) [:p.info flash])
            [:h3 "Your plan detail"]
            (let [parts (plan/plan-with-ics-record ics-record)
                  colcount (max-cols parts)]
              (map-indexed #(view-part %1 %2 colcount) parts))))))
