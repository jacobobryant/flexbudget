(ns bud.backend.env
  (:require [datomic.client.api :as d]
            [bud.shared.schema :refer [datomic-schema]]
            [datomic.ion :refer [get-env]]
            [jobryant.util :as u]
            [mount.core :refer [defstate start]]))

(u/load-fns
  jobryant-connect jobryant.datomic.api/connect
  jobryant-transact jobryant.datomic.api/transact
  jobryant-storage-path! jobryant.datomic.api/storage-path!
  memdb-client compute.datomic-client-memdb.core/client
  memdb-localdb compute.datomic-client-memdb.core/->LocalDb)

(def config
  (merge
    {:env :prod
     :db-name "dev"
     :client-cfg {:server-type :ion
                  :region "us-east-1"
                  :system "bud"
                  :endpoint "http://entry.bud.us-east-1.datomic.net:8182/"
                  :proxy-port 8182}}
    (get-env)))

(def ^:private dev? (= :dev (:env config)))

(defstate client :start
  ((if dev? memdb-client d/client) (:client-cfg config)))

(defstate conn :start
  (let [with-args (fn [f] (f client (select-keys config [:db-name])))]
    (if dev?
      (do
        (jobryant-storage-path! "storage.edn")
        (jobryant-connect
          (str "datomic:mem://" (:db-name config))
          {:schema datomic-schema
           :tx-fn-ns 'bud.backend.tx})
        (with-args d/connect))
      (do
        (with-args d/create-database)
        (let [conn (with-args d/connect)]
          (d/transact conn {:tx-data datomic-schema})
          conn)))))

(when (not dev?) (start))

(def wrap-db (if dev? #(memdb-localdb % (:db-name config)) identity))

(def transact
  (if dev?
    (fn [conn arg-map]
      (let [f #(memdb-localdb % (:db-name config))]
        (-> @(jobryant-transact (.-conn conn) (:tx-data arg-map))
            (update :db-before f)
            (update :db-after f))))
    d/transact))
