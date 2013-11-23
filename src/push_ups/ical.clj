(ns push-ups.ical
  (:use [push-ups.plan :only (plan-with-ics-record)]
        [clj-time.format :only (unparse formatters)]
        [clj-time.core :only (plus days hours)]
        [clojure.string :only (join)]
        [clojure.pprint :only (pprint)]))

(defn- utc-time-str
  [date]
  (unparse (formatters :basic-date-time-no-ms)
           date))

(defn- generate-summary
  [sets]
  (format "Push-up exercise of %d sets" (inc (count sets))))

(defn- generate-description
  [sets rest_ end]
  (->>
    (interleave 
      (map-indexed #(format "+ SET%d(%s push-ups)\\n" (inc %1) %2) sets)
      (map #(format "  REST(%s seconds)\\n\\n" %) (repeat rest_)))
    (apply str)
    (#(format "%s+ MAX (>= %d push-ups)" % end))))

(defn- todos-for-part
  "Generate todos with part"
  [part]
  (join "\n\n"
        (map 
          (fn [event]
            (let [date (:date event)
                  sets (:sets event)
                  end (:end event)
                  rest_ (:rest event)
                  summary (generate-summary sets)
                  description (generate-description sets rest_ end)]
              (join "\n"
                    ["BEGIN:VEVENT"
                     (str "DTSTART:" (utc-time-str date))
                     (str "DTEND:" (utc-time-str (plus date (hours 1))))
                     (str "SUMMARY:" summary)
                     (str "DESCRIPTION:" description)
                     "END:VEVENT"])))
          part)))

(defn ical-with-ics-record
  "Generate ics with ics-record"
  [ics-record]
  (let [plan (plan-with-ics-record ics-record)]
    (str "BEGIN:VCALENDAR\nVERSION:2.0\n\n"
         (join "\n"
               (map 
                 (fn [part]
                   (let [last-event (last part)
                         date (plus (:date last-event) (days 1))
                         url (str "/i/" (:permalink ics-record))]
                     (str (todos-for-part part)
                          "\n\n"
                          (join "\n"
                                ["BEGIN:VEVENT"
                                 (str "DTSTART:" (utc-time-str date))
                                 (str "DTEND:" (utc-time-str (plus date (hours 1))))
                                 (str "SUMMARY:Its time for "
                                      (if (= 3 (count part)) "final" "periodic")
                                      " test. Log your result at " url)
                                 "END:VEVENT"])))) 
                 plan))
         "\n\nEND:VCALENDAR")))
