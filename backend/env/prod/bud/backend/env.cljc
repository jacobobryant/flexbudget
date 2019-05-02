(ns bud.backend.env
  (:require [datomic.client.api :as d]))

(def overrides {})

(defn connect [client db-name schema]
  (d/create-database client {:db-name db-name})
  (let [conn (d/connect client {:db-name db-name})]
    (d/transact conn {:tx-data schema})
    conn))
