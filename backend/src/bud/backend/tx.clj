(ns bud.backend.tx
  (:require [bud.backend.authorizers :refer [authorizers]]
            [jobryant.datomic-cloud.txauth :as txauth]
            [datomic.client.api :as d]))

(defn authorize [db uid tx]
  (txauth/authorize authorizers db uid tx))

; good for testing failing txes
#_(defn foo []

  (let [uid "WIg660MbESckhLHxii9ciq0LJMy1"
        tx [{:db/id 22803871160074320
             :entry/asset [{:db/id "tmp"
                            :misc/amount 0
                            :misc/description ""}]}]
        db (d/with-db bud.backend.env/conn)]
    #_(d/pull db '[*] 22803871160074320)
    (txauth/authorize authorizers db uid tx)))
