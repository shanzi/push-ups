(ns push-ups.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [push-ups.web :as web]))

(defroutes app-routes
           (GET "/" [] (web/index))
           (GET "/new" [] (web/new-plan))
           (PUT "/new" {params :params flash :flash} (web/new-plan params flash))
           (route/resources "/")
           (route/not-found "Not Found"))


(def app
  (handler/site app-routes))
