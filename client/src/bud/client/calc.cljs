(ns bud.client.calc
  (:require [cljs-time.core :refer [plus weeks months years before? today in-days interval]]))

(defn forecast [goal-date
                {amount :misc/amount
                 basis :delta/basis
                 frequency :delta/frequency}]
  (let [frequency (case frequency
                    :weekly (weeks 1)
                    :monthly (months 1)
                    :yearly (years 1))
        today (today)]
    (* amount
       (->> (iterate #(plus % frequency) basis)
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
