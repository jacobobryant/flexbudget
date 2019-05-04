(ns jobryant.util.core
  (:require [clojure.walk :refer [postwalk]]
            [clojure.pprint]
            [clojure.spec.alpha :as s]
 #?@(:cljs [[goog.string :as gstring]
            [goog.string.format]
            [cljs.core.async :refer [<! put! chan close!]]]
      :clj [[potemkin :refer [import-vars]]]))
  #?(:cljs (:require-macros jobryant.util.core)))

(def pprint clojure.pprint/pprint)

#?(:clj (do

(defmacro capture [& xs]
  `(do ~@(for [x xs] `(def ~x ~x))))

(defmacro pullall [& nses]
  `(import-vars
     ~@(for [n nses]
         (into [n] (keys (ns-publics n))))))

(defmacro cljs-pullall [nspace & syms]
  `(do ~@(for [s syms]
           `(def ~s ~(symbol (str nspace) (str s))))))

(defmacro forv [& body]
  `(vec (for ~@body)))

(defmacro condas->
  "Combination of as-> and cond->."
  [expr name & clauses]
  (assert (even? (count clauses)))
  `(as-> ~expr ~name
     ~@(map (fn [[test step]] `(if ~test ~step ~name))
            (partition 2 clauses))))

; todo move instead of copy
(defn move [src dest]
  (spit dest (slurp src)))

(defn manhattand [a b]
  (->> (map - a b)
       (map #(Math/abs %))
       (apply +)))

(defmacro js<! [form]
  `(cljs.core.async/<! (to-chan ~form)))

(defmacro for-every? [& forms]
  `(every? boolean (for ~@forms)))

(defmacro for-some? [& forms]
  `(some boolean (for ~@forms)))

(defn loadf [sym]
  (fn [& args]
    (require (symbol (namespace sym)))
    (let [f (if-let [f (resolve sym)]
              f
              (throw (ex-info sym " is not on the classpath.")))]
      (apply @f args))))

(defmacro load-fns [& forms]
  (assert (even? (count forms)))
  `(do ~@(for [[sym fn-sym] (partition 2 forms)]
           `(def ~sym (#'loadf (quote ~fn-sym))))))

))

(defn pred-> [x f g]
  (if (f x) (g x) x))

#?(:cljs (do

(defn to-chan [p]
  (let [c (chan)]
    (.. p (then #(put! c %))
        (catch #(do
                  (.error js/console %)
                  (close! c))))
    c))

(def format gstring/format)

))

(def ^:private instant-type #?(:cljs (type #inst "0001-01-01")
                               :clj java.util.Date))
(defn instant? [x]
  (= (type x) instant-type))

(defn indexed [xs]
  (map-indexed vector xs))

(defn map-from
  [f xs]
  (into {} (for [x xs] [x (f x)])))

(defn dissoc-by [m f]
  (into {} (remove (comp f second) m)))

(defn map-inverse [m]
  (reduce
    (fn [inverse [k v]]
      (update inverse v
              #(if (nil? %)
                 #{k}
                 (conj % k))))
    {}
    m))

(defn conj-some [coll x]
  (cond-> coll
    x (conj x)))

(defn assoc-some [m k v]
  (cond-> m (some? v) (assoc k v)))

(defn split-by [f coll]
  (reduce
    #(update %1 (if (f %2) 0 1) conj %2)
    [nil nil]
    coll))

(defn deep-merge [& ms]
  (apply
    merge-with
    (fn [x y]
      (cond (map? y) (deep-merge x y)
            :else y))
    ms))

(defn remove-nil-empty [m]
  (into {} (remove (fn [[k v]]
                     (or (nil? v)
                         (and (coll? v) (empty? v)))) m)))

(defn remove-nils [m]
  (into {} (remove (comp nil? second) m)))

(defn deep-merge-some [& ms]
  (postwalk (fn [x]
              (if (map? x)
                (remove-nil-empty x)
                x))
            (apply deep-merge ms)))

(defn merge-some [& ms]
  (reduce
    (fn [m m']
      (let [[some-keys nil-keys] (split-by (comp some? m') (keys m'))]
        (as-> m x
          (merge x (select-keys m' some-keys))
          (apply dissoc x nil-keys))))
    ms))

#?(:cljs
(def char->int (into {} (map #(vector (char %) %) (range 256))))
)

(defn ord [c]
  (#?(:clj int :cljs char->int) c))

(defn parse-int [s]
  (#?(:clj Integer/parseInt :cljs js/parseInt) s))

; Do this with algo.generic
(defn cop [op & cs]
  (char (apply op (map ord cs))))

(defn c+ [& cs]
  (apply cop + cs))

(defn c- [& cs]
  (apply cop - cs))

(defn zip [xs]
  (apply map vector xs))

(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))
