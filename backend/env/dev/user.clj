(ns user
  (:require [clojure.tools.namespace.repl :as tn]
            [mount.core :as mount]
            [nrepl.server :refer [start-server]]
            [orchestra.spec.test :as st]
            [datomic.client.api :as d]
            [bud.core]))

(st/instrument)

(def cfg {:server-type :ion
          :region "us-east-1"
          :system "bud"
          :endpoint "http://entry.bud.us-east-1.datomic.net:8182/"
          :proxy-port 8182})

(defn get-client []
  (d/client cfg))

(comment
  (nrepl.server/start-server :port 7888)

  ; for un-botching the repl
  (require '[clojure.tools.namespace.repl :as tn])
  (tn/set-refresh-dirs "src")
  (tn/refresh)

)

(tn/set-refresh-dirs "src")

(defn nrepl []
  (start-server :port 7888))

(defn go []
  (mount/start)
  :ready)

(defmacro reset []
  `(do (mount/stop)
       (tn/refresh :after 'user/go)
       (use 'clojure.repl)))
