(ns user
  (:require [datomic.client.api :as d]
            [bud.core]))

(def cfg {:server-type :ion
          :region "us-east-1" ;; e.g. us-east-1
          :system "bud"
          :endpoint "http://entry.bud.us-east-1.datomic.net:8182/"
          :proxy-port 8182})

(defn get-client []
  (d/client cfg))
