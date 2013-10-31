(ns push-ups.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [push-ups.web :as web]))

(defroutes app-routes
  (GET "/" [] (web/index))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
