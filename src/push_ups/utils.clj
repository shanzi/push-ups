(ns push-ups.utils
  (:use [clojure.string :only (join)]))

(defn domain
  "Get domain of app"
  ([]
   (or (System/getenv "HOST")
       "http://localhost:3000"))
  ([& pathcomponents]
     (join "/" (cons (domain) pathcomponents))))
