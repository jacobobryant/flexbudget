(ns bud.backend.env
  (:require [compute.datomic-client-memdb.core :as memdb]
            [jobryant.datomic.api :as d]
            [datomic.client.api :as client]))

(d/storage-path! "storage.edn")

;todo get name correctly
(defn transact [conn arg-map]
  (-> @(d/transact (.-conn conn) (:tx-data arg-map))
      (update :db-before #(memdb/->LocalDb % "dev"))
      (update :db-after #(memdb/->LocalDb % "dev"))))

(def overrides
  {:db-name "dev"
   :client-fn memdb/client
   :transact-fn transact
   :client-cfg {}
   :wrap-db-fn #(memdb/->LocalDb % "dev")})

(defn connect [client db-name schema]
  (d/connect (str "datomic:mem://" db-name)
             {:schema schema
              :tx-fn-ns 'bud.backend.tx})
  (client/connect client {:db-name db-name}))
