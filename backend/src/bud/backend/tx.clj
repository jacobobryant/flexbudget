(ns bud.backend.tx
  (:require [bud.backend.authorizers :refer [authorizers]]
            [bud.backend.env :refer [with]]
            [jobryant.txauth :as txauth]
            [datomic.client.api :as d]))

(defn authorize [db uid tx]
  (txauth/authorize authorizers with db uid tx))
