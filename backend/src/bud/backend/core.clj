(ns bud.backend.core
  (:require [trident.web :as web]
            [trident.util :as u] ; also needed for data_readers
            [trident.ring :as tring]
            [bud.backend.authorizers :refer [authorizers]]
            [bud.backend.query :refer [datoms-for]]
            [datomic.ion.cast :as cast]
            [bud.shared.schema :refer [schema]]
            [datomic.ion.lambda.api-gateway :as apigw]
            [mount.core :as mount]))

(mount/defstate handler*
  :start (web/init!
           (merge
             {:env :dev
              :db-name "dev"
              :app-name "bud"
              :origins [#"http://dev.notjust.us:8000"
                        #"https://notjust.us"
                        #"https://www.notjust.us"]
              :authorizers `authorizers
              :datoms-for datoms-for
              :local-tx-fns? true
              :schema schema}
             (u/read-config "bud-config.edn"))))

(def handler (apigw/ionize
               (tring/wrap-catchall
                 #(do
                    (cast/event {:msg "flexbudget starting mount"})
                    (mount/start)
                    (cast/event {:msg "flexbudget started mount"})
                    (handler* %)))))



(comment

  (require '[datomic.client.api :as d])

  (.printStackTrace *e)

  (let [config (merge
                 {:env :dev
                  :db-name "dev"
                  :app-name "bud"
                  :origins [#"http://dev.notjust.us:8000"
                            #"https://notjust.us"
                            #"https://www.notjust.us"]
                  :authorizers `authorizers
                  :datoms-for datoms-for
                  :schema schema}
                 #_(u/read-config "bud-config.edn"))
        conn (trident.datomic-cloud/init-conn (assoc config :client-cfg (trident.ion/default-config)))
        uid "WIg660MbESckhLHxii9ciq0LJMy1"
        tx [{:db/id 22803871160074468, :misc/amount 4207, :misc/description "cash"}]]
    ;(trident.datomic-cloud.txauth/authorize (d/with-db conn) `authorizers uid tx))
    (datomic.client.api/transact conn {:tx-data [['trident.datomic-cloud.txauth/authorize `authorizers uid tx]]}))
    ;(datomic.client.api/transact conn {:tx-data tx}))


)
