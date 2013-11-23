(ns push-ups.handler
  (:use compojure.core
        [ring.util.response :only (not-found)])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [push-ups.web :as web]
            [ring.adapter.jetty :as jetty]))

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
  (GET "/c/:permalink" [permalink] (safe404 (web/calendar permalink)))
  (route/resources "/statics/")
  (route/not-found "Not Found"))


(def app
  (handler/site app-routes))

(defn -main
  [port]
  (jetty/run-jetty app {:port (Integer. port) :join? false}))
