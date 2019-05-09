(ns bud.client.event
  (:require [jobryant.util :refer [js<!]]))

(defmacro request
  ([method uri payload]
   `(cljs.core.async/<!
      (request* ~method ~uri ~payload (js<! (bud.client.db/token)))))
  ([method uri]
   `(request ~method ~uri {})))
