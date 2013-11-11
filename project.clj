(defproject push-ups "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/java.jdbc "0.3.0-alpha5"]
                 [org.clojure/tools.logging "0.2.6"]
                 [postgresql/postgresql "8.4-702.jdbc4"]
                 [korma "0.3.0-RC5"]
                 [compojure "1.1.5"]
                 [clj-time "0.6.0"]
                 [hiccup "1.0.4"]]
  :plugins [[lein-ring "0.8.5"]]
  :ring {:handler push-ups.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.5"]
                        [org.xerial/sqlite-jdbc "3.7.2"]]}})
