(ns bud.backend.tx
  (:require [bud.backend.authorizers :refer [authorizers]]
            [bud.backend.config :as c]
            [jobryant.txauth :as txauth]))

(defn authorize [db uid tx]
  (txauth/authorize authorizers (c/wrap-db db) uid tx))
