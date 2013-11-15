(ns push-ups.db
  (:use [clojure.java.jdbc :only (with-connection create-table)]
        korma.db
        korma.core
        [clj-time.coerce :only (from-string)]
        [clj-time.core :only (to-time-zone time-zone-for-offset)])
  (:require clojure.pprint
            [clojure.tools.logging :as log]
            [clojure.string :as string])
  (:import java.util.UUID
           (java.net URI)))

(def db 
  (if-let [url (System/getenv "HEROKU_POSTGRESQL_RED_URL")]
    (let [db-uri (java.net.URI. url)]
      (->> (string/split (.getUserInfo db-uri) #":")
           (#(identity {:db (last (string/split url #"\/"))
                        :host (.getHost db-uri)
                        :port (.getPort db-uri)
                        :user (% 0)
                        :password (% 1)
                        :ssl true}))
           (postgres)))
    (sqlite3 {:db "push-ups.db"})))


(defn setup
  "setup database"
  []
  (with-connection db
                   (create-table :ics_records
                                 [:permalink "varchar(15)" "PRIMARY KEY"]
                                 [:part_1_test_r :integer]
                                 [:part_1_date :datetime]
                                 [:part_2_test_r :integer]
                                 [:part_2_date :datetime]
                                 [:part_3_test_r :datetime]
                                 [:part_3_date :datetime]
                                 [:final_test_r :integer])))


(defentity ics-records
           (table :ics_records)
           (database db))

(defmacro dbsafe
  "catch errors throwed by db, avoid interruption"
  [& forms]
  (let [esym (gensym) ret (gensym)]
  `(try 
     (let [~ret (do ~@forms)]
       (when (seq ~ret) ~ret))
     (catch Exception ~esym
       (do 
         (log/error ~esym "db operation failed")
         false)))))

(defn gen-permalink
  "generate a unique permalink for ics-record"
  []
  (format "%x" (.hashCode (java.util.UUID/randomUUID))))

(defn- pad-string->num 
  [string]
  (read-string
    (if (= (first string) \0)
      (str (last string))
      string)))

(defn from-string-tz
  "convert string into date with timezone"
  [string]
  (let [tz (re-seq #"(\+|-)(\d\d):(\d\d)" string)]
    (when-let [[_ s h m] (first tz)]
      (to-time-zone (from-string string)
                    (case s
                      "-" (time-zone-for-offset (- (pad-string->num h)) (pad-string->num m))
                      "+" (time-zone-for-offset (pad-string->num h) (pad-string->num m)))))))

(defn new-ics-record
  "create a new ics record and insert into database"
  [permalink initial-test-result start-date]
  (dbsafe
    (insert ics-records 
            (values {:permalink permalink
                     :part_1_test_r initial-test-result
                     :part_1_date (str start-date)}))))

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
             {k (from-string-tz v)}
             {k v}))))
      (into {}))))


(defn update-ics-record
  "Update ics record with specified permalink"
  [permalink values]
  (dbsafe
    (update ics-records
            (set-fields (into {} (map (fn [[k v]]
                                        (if (and v (.endsWith (str k) "date"))
                                          {k (str v)}
                                          {k v}))
                                      values)))
            (where {:permalink permalink}))))
