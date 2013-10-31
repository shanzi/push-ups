(ns push-ups.db
  (:use [clojure.java.jdbc :only (with-connection create-table)]
        clojureql.core)
  (:import java.util.UUID))

(def db (if (System/getenv "DATABASE_URL")
          (System/getenv "DATABASE_URL")
          {:classname "org.sqlite.JDBC"
           :subprotocol "sqlite"
           :subname "push-ups.db"}))

(def ics-records (table db :ics_records) )

(defn create-tables
  "Create sql tables"
  []
  (with-connection db
                   (create-table :ics_records
                                 [:permalink "varchar(15)" "PRIMARY KEY"]
                                 [:initial_test_r :integer]
                                 [:part_1_date :datetime]
                                 [:part_1_test_r :integer]
                                 [:part_2_date :datetime]
                                 [:part_2_test_r :integer]
                                 [:part_3_date :datetime]
                                 [:final_test_r :integer]
                                 )))



(defn gen-permalink
  "generate a unique permalink for ics-record"
  []
  (format "%x" (.hashCode (java.util.UUID/randomUUID))))

(defn new-ics-record
  "create a new ics record and insert into database"
  [permalink initial_test_result start_date]
  (conj! ics-records {:permalink permalink
                               :initial_test_r initial_test_result
                               :part_1_date start_date}))

(defn get-ics-record
  "get ics record with permalink"
  [permalink]
  (-> @(select ics-records (where (= :permalink permalink)))
    (first)))

