(ns bud.client.calc
  (:require [cljs-time.core :refer [plus weeks months years before? today in-days interval]]))

(defn frequency [freq-keyword]
  (case freq-keyword
    :weekly (weeks 1)
    :monthly (months 1)
    :yearly (years 1)))

(defn days-in [freq-keyword]
  (in-days (interval (today) (plus (today) (frequency freq-keyword)))))

(defn forecast [goal-date
                {amount :misc/amount
                 basis :delta/basis
                 freq :delta/frequency}]
  (let [freq (frequency freq)
        today (today)]
    (* amount
       (->> (iterate #(plus % freq) basis)
            (drop-while #(before? % today))
            (take-while #(before? % goal-date))
            count))))

(defn weekly-allowance [forecasted goal]
  (let [today (today)
        remaining (- forecasted (:misc/amount goal))
        days-left (in-days (interval today (:goal/date goal)))]
    (* (/ remaining days-left) 7)))

(defn surplus [forecasted goal]
  (let [today (today)
        remaining (- forecasted (:misc/amount goal))
        weeks-left (/ (in-days (interval today (:goal/date goal))) 7)
        allowance-left (* (:goal/allowance goal) weeks-left)]
    (- remaining allowance-left)))
