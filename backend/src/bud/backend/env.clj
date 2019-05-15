(ns bud.backend.env
  (:require [datomic.client.api :as d]
            [bud.shared.schema :refer [datomic-schema]]
            [datomic.ion :as ion]
            [jobryant.util :as u]
            [jobryant.datomic-cloud.client :refer [connect]]
            [mount.core :refer [defstate start]]
            [clojure.walk :refer [keywordize-keys]]))

(def config
  (merge
    {:env :dev
     :db-name "dev"
     :client-cfg {:server-type :ion
                  :region "us-east-1"
                  :system "bud"
                  :endpoint "http://entry.bud.us-east-1.datomic.net:8182/"
                  :proxy-port 8182}
     :local-tx-fns? false}
    (ion/get-env)))

(def get-params (memoize #(-> {:path (str "/datomic-shared/" (name %) "/bud/")}
                              ion/get-params
                              keywordize-keys)))

(defn get-param [k]
  (->> [(name (:env config)) "default"]
       (map #(get (get-params %) k))
       (filter some?)
       first))

(def ^:private dev? (= :local (:env config)))

(defstate client :start (d/client (:client-cfg config)))

(defstate conn :start
  (do
    (d/create-database client (select-keys config [:db-name]))
    (let [conn (connect client (select-keys config [:db-name :local-tx-fns?]))]
      (d/transact conn {:tx-data datomic-schema})
      conn)))
