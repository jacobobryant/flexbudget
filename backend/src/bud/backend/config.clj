(ns bud.backend.config
  (:require [datomic.client.api :as d]))

(def db-name "prod")
(def client-fn d/client)
(def client-cfg {:server-type :ion
                 :region "us-east-1"
                 :system "bud"
                 :endpoint "http://entry.bud.us-east-1.datomic.net:8182/"
                 :proxy-port 8182})
