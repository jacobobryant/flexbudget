(ns bud.client.event
  (:require [jobryant.util :refer [js<!]]
            [bud.shared.config :as c]))

(defmacro request
  ([method uri payload]
   `(cljs.core.async/<!
      (~method (str c/backend-host ~uri)
               (merge {:with-credentials? false
                       :oauth-token (js<! (bud.client.db/token))}
                      ~payload))))
  ([method uri]
   `(request ~method ~uri {})))
