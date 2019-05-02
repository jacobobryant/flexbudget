(ns bud.backend.config
  (:require [datomic.client.api :as d]
            [bud.backend.env :as env]
            [bud.shared.schema :refer [datomic-schema]])
  (:import (com.google.firebase FirebaseApp FirebaseOptions$Builder)
           com.google.auth.oauth2.GoogleCredentials))

(def conf
  (merge
    {:db-name "prod"
     :client-fn d/client
     :transact-fn d/transact
     :client-cfg {:server-type :ion
                  :region "us-east-1"
                  :system "bud"
                  :endpoint "http://entry.bud.us-east-1.datomic.net:8182/"
                  :proxy-port 8182}
     :wrap-db-fn identity}
    env/overrides))

(when (= 0 (count (FirebaseApp/getApps)))
  (let [options (-> (new FirebaseOptions$Builder)
                    (.setCredentials (GoogleCredentials/getApplicationDefault))
                    (.setDatabaseUrl "https://budget-6fc5c.firebaseio.com")
                    .build)]
    (FirebaseApp/initializeApp options)))

(def client
  (memoize #((:client-fn conf) (:client-cfg conf))))

(def conn
  (memoize #(env/connect (client) (:db-name conf) datomic-schema)))

(def transact (:transact-fn conf))

(def wrap-db (:wrap-db-fn conf))
