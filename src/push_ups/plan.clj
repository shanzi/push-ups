(ns push-ups.plan
  (:use [clj-time.core :only (days plus)]))

(def exercise-sets 
  {:part_1 [[[2 3 2 2 3] [3 4 2 3 4] [4 5 4 4 5] [4 6 4 4 6] [5 6 4 4 7] [5 7 5 5 8]]
            [[6 6 4 4 5] [6 8 6 8 7] [8 10 7 7 10] [9 11 8 8 11] [10 12 9 9 13] [12 13 10 10 15]]
            [[10 12 7 7 9] [10 12 8 8 12] [11 15 9 9 13] [14 14 10 10 15] [14 16 12 12 17] [16 17 14 14 20]]]

   :part_2 [[[10 12 7 7 9] [10 12 8 8 12] [11 13 9 9 13] [12 14 11 10 16] [14 16 12 12 12] [16 18 13 13 20]]
            [[12 17 13 13 17] [14 19 14 14 19] [16 21 15 15 21] [18 22 16 16 25] [20 25 20 20 28] [23 28 23 23 33]]
            [[14 18 14 14 20] [20 15 15 15 25] [22 30 20 20 28] [21 25 21 21 32] [25 19 25 25 36] [29 33 29 29 40]]]

   :part_3 [[[17 19 15 15 20] [10 10 13 13 10 10 9 25] [13 15 15 12 12 10 30]
             [25 30 20 15 40] [14 14 15 15 14 14 10 10 44] [13 13 17 17 16 16 14 14 50]]
            [[28 35 25 22 35] [18 18 20 20 14 14 16 40] [18 18 20 20 17 17 20 45]
             [40 50 25 25 50] [20 20 23 23 20 20 18 18 53] [22 22 30 30 15 25 18 18 55]]
            [[36 40 30 24 40] [19 19 22 22 18 18 22 45] [20 20 24 24 20 20 22 50]
             [45 55 35 39 55] [22 22 30 30 24 24 18 18 58] [26 26 33 33 26 26 22 22 60]]]})



(defn- plan-for-part
  [part date result]
  (let [sets (get exercise-sets part)]
    (if (nil? date)
      nil
      (if (and (= part :part_1) (>= result 3))
        (plan-for-part :part_2 date (- result 2))
        (into []
              (map-indexed
                (fn [idx sets]
                  {:date (plus date
                               (days (if (< idx 3)
                                       (* idx 2)
                                       (+ (* idx 2) 1))))
                   :sets (butlast sets) 
                   :end (last sets)
                   :rest (if (= part :part_3)
                           (nth [60 45 45] (mod idx 3))
                           (nth [60 90 120] (mod idx 3)))
                   }
                  )
                (nth (part exercise-sets) result)))))))

(defn plan-with-ics-record
  [ics-record]
  (into [] 
        (filter #(not (nil? %))
                (map (fn [partstr]
                       (let [part (keyword partstr)
                             date ((keyword (str partstr "_date")) ics-record)
                             result ((keyword (str partstr "_test_r")) ics-record)]
                         (plan-for-part part date result)))
                     ["part_1", "part_2", "part_3"])))) 
