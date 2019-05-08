(ns bud.backend.env
  (:require [datomic.client.api :as d]
            [bud.shared.schema :refer [datomic-schema]]
            [datomic.ion :as ion]
            [jobryant.util :as u]
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
  (let [with-args #(% client (select-keys config [:db-name]))]
    (do
      (with-args d/create-database)
      (let [conn (with-args d/connect)]
        (d/transact conn {:tx-data datomic-schema})
        conn))))

(def transact (if (:local-tx-fns? config)
                (let [lock (Object.)]
                  (fn [conn arg-map]
                    (locking lock
                      (->> #(u/eval-txes (d/with-db conn) %)
                           (update arg-map :tx-data)
                           (d/transact conn)))))
                d/transact))

(def with (if (:local-tx-fns? config)
            (fn [db arg-map]
              (u/capture db arg-map)
              (->> #(u/eval-txes db %)
                   (update arg-map :tx-data)
                   (d/with db)))
            d/with))
