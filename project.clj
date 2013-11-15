(defproject push-ups "0.1.0-SNAPSHOT"
  :description "A simple app to help you generate push-ups exercise plan."
  :url "https://github.com/shanzi/push-ups"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/java.jdbc "0.3.0-alpha5"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [ring/ring-jetty-adapter "1.1.6"]
                 [postgresql/postgresql "8.4-702.jdbc4"]
                 [korma "0.3.0-RC6"]
                 [compojure "1.1.5"]
                 [clj-time "0.6.0"]
                 [hiccup "1.0.4"]]
  :uberjar-name "push-ups.jar"
  :min-lein-version "2.0.0"
  :plugins [[lein-ring "0.8.5"]]
  :ring {:handler push-ups.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]
                        [org.clojure/tools.trace "0.7.6"]]}})
