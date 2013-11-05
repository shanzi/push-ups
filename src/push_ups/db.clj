(ns push-ups.db
  (:use [clojure.java.jdbc :only (with-connection create-table)]
        korma.db
        korma.core
        [clj-time.format :only (formatters parse unparse)])
  (:require clojure.pprint)
  (:import java.util.UUID))

(def db (if (System/getenv "DATABASE_URL")
          (postgres {:db (System/getenv "DATABASE_URL")})
          (sqlite3 {:db "push-ups.db"})))


(defn setup
  "setup database"
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


(defentity ics-records
           (table :ics_records)
           (database db))

(defn gen-permalink
  "generate a unique permalink for ics-record"
  []
  (format "%x" (.hashCode (java.util.UUID/randomUUID))))

(defn str-to-date
  [string]
  (parse (formatters :date-time) string))

(defn new-ics-record
  "create a new ics record and insert into database"
  [permalink initial_test_result start_date]
  (insert ics-records 
          (values {:permalink permalink
                   :initial_test_r initial_test_result
                   :part_1_date (str start_date)})))

(defn get-ics-record
  "get ics record with permalink"
  [permalink]
  (->> (select ics-records 
              (where {:permalink permalink}))
    (first)
    ((fn [entity]
       (for [[k v] entity]
         (if (and v (.endsWith (str k) "date"))
             {k (str-to-date v)}
             {k v}))))
    (into {})))

(defn update-ics-record
  "Update ics record with specified permalink"
  [permalink values]
  (update ics-records
          (set-fields values)
          (where {:permalink permalink})))
