(ns jobryant.views
  (:require [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn get-opts [opts contents]
  (if (map? opts)
    [opts contents]
    [nil (conj contents opts)]))

(defmacro defview [f [opts contents] & forms]
  `(defn ~f [opts# & contents#]
     (let [[~opts ~contents] (get-opts opts# contents#)]
       ~@forms)))

(defview form [opts contents]
  [:form (merge {:method "post"} opts)
   (anti-forgery-field)
   contents])

(defn post-button [action text]
  (form action [:input {:type "submit" :value text}]))
