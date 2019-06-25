(ns bud.backend.core
  (:require [trident.web :as web]
            [trident.util :as u] ; also needed for data_readers
            [trident.ring :as tring]
            [bud.backend.authorizers :refer [authorizers]]
            [bud.backend.query :refer [datoms-for]]
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
              :schema schema}
             (u/read-config "config.edn"))))

(def handler (apigw/ionize
               (tring/wrap-catchall
                 #(do
                    (mount/start)
                    (handler* %)))))
