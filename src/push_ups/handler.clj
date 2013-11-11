(ns push-ups.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [push-ups.web :as web]))

(defroutes app-routes
           (GET "/" [] (web/index))
           (GET "/new" [] (web/new-plan))
           (PUT "/new" {params :params flash :flash} (web/new-plan params flash))
           (context "/i/:permalink" [permalink]
                    (GET "/" {flash :flash} (web/view-plan permalink flash)))
           (route/resources "/statics/")
           (route/not-found "Not Found"))


(def app
  (handler/site app-routes))
