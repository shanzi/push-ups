(ns push-ups.web
  (:use [hiccup.page]
        [hiccup.element])
  (:require [push-ups.forms :as forms]))


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
      [:div.footer "xiuxiu.de (c) 2013"]]]))


(defn index []
  "index template"
  (base "push-ups calendar"
        [:p "This website is designed to help create calendar events 
            for your push-up exercise according to the instructions from "
         (link-to "http://www.hundredpushups.com/" "handredspushups.com") "."]
        [:p "No need to register, just fill in the form below, you will get an ics format
            calendar subscription source."]
        [:p.initial-form
         (forms/initial-form "/new")]))
