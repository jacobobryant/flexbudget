(ns jobryant.re-com.core
  (:refer-clojure :exclude [for]))

(defmacro for [bindings body]
  `(clojure.core/for ~bindings
     (with-meta ~body {:key ~(first bindings)})))

(defn gap []
  [:div {:style {:width "10px"
                 :height "10px"}}])

(defn v-box [& children]
  (into
    [:div {:style {:display "flex"
                   :flex-direction "column"}}]
    (interpose (gap) (filter some? children))))
