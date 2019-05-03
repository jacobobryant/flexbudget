(ns bud.backend.tx
  (:require [bud.backend.authorizers :refer [authorizers]]
            [bud.backend.env :refer [wrap-db]]
            [jobryant.txauth :as txauth]))

(defn authorize [db uid tx]
  (txauth/authorize authorizers (wrap-db db) uid tx))
