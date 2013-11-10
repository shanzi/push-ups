(ns push-ups.web
  (:use hiccup.page
        hiccup.element
        [ring.util.response :only (redirect)])
  (:require [push-ups.forms :as forms]
            clojure.pprint))


(defn base
  "base html frameworks"
  [title & content]
  (html5
    (include-css "base.css")
    [:head [:title title]]
    [:body
     [:div#frame
      (image "logo.png" "logo")
      [:div.content content]
      [:div.footer "xiuxiu.de (c) 2013"]]
     (include-js "base.js")]))


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
   (if (nil? params)
     (base "Create Exercise Plan"
           [:h1 "Create a new exercise plan"]
           (when-not (nil? flash)
             [:p.error flash])
           (forms/initial-form ""))
     (let [start-date (forms/parse-start-date-time params)
           test-result (forms/parse-test-result params)]
       (if (nil? start-date)
         (new-plan nil "There are errors in your form, please correct them")
         (redirect "/"))))))
