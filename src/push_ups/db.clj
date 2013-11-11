(ns push-ups.db
  (:use [clojure.java.jdbc :only (with-connection create-table)]
        korma.db
        korma.core
        [clj-time.coerce :only (from-sql-date to-sql-date)])
  (:require clojure.pprint
            [clojure.tools.logging :as log])
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

(defmacro dbsafe
  "catch errors throwed by db, avoid interruption"
  [& forms]
  (let [esym (gensym)]
  `(try 
     (do ~@forms)
     (catch Exception ~esym
       (do 
         (log/error ~esym "db operation failed")
         false)))))

(defn new-ics-record
  "create a new ics record and insert into database"
  [permalink initial-test-result start-date]
  (dbsafe
    (insert ics-records 
            (values {:permalink permalink
                     :initial_test_r initial-test-result
                     :part_1_date (to-sql-date start-date)}))))

(defn get-ics-record
  "get ics record with permalink"
  [permalink]
  (dbsafe
    (->> (select ics-records 
                 (where {:permalink permalink}))
      (first)
      ((fn [entity]
         (for [[k v] entity]
           (if (and v (.endsWith (str k) "date"))
             {k (from-sql-date v)}
             {k v}))))
      (into {}))))

(defn update-ics-record
  "Update ics record with specified permalink"
  [permalink values]
  (dbsafe
    (update ics-records
            (set-fields (into {} (map (fn [[k v]]
                                        (if (and (not (nil? v)  (.endsWith (str k) "date")))
                                          {k (to-sql-date v)}
                                          {k v}))
                                      values)))
            (where {:permalink permalink}))))
