(ns push-ups.handler
  (:use compojure.core
        [ring.util.response :only (not-found)])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [push-ups.web :as web]))

(defn- safe404
  [ret]
  (if ret
    ret (not-found "Not found")))

(defroutes app-routes
  (GET "/" [] (web/index))
  (context "/new" []
           (GET "/" [] (web/new-plan))
           (POST "/" {params :params flash :flash} (web/new-plan params flash)))
  (context "/i/:permalink" [permalink]
           (GET "/" {flash :flash} (safe404 (web/view-plan permalink flash)))
           (POST "/" {params :params} (safe404 (web/record-test-result permalink params))))
  (route/resources "/statics/")
  (route/not-found "Not Found"))


(def app
  (handler/site app-routes))
