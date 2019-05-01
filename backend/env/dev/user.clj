(ns user
  (:require [clojure.tools.namespace.repl :as tn]
            [mount.core :as mount :refer [defstate]]
            [nrepl.server :refer [start-server]]
            [orchestra.spec.test :as st]
            [datomic.client.api :as d]
            [jobryant.util :as u]
            [compute.datomic-client-memdb.core :as memdb]
            [bud.backend.core :as core]
            [bud.backend.config :as c]
            [aleph.http :as aleph]))

(comment
  (nrepl.server/start-server :port 7888)

  ; for un-botching the repl
  (require '[clojure.tools.namespace.repl :as tn])
  (tn/set-refresh-dirs "src")
  (tn/refresh)

)

(st/instrument)

(defn start-aleph []
  (with-redefs [c/db-name "dev"
                c/client-fn memdb/client
                c/client-cfg {}]
    (aleph/start-server
      core/handler'
      {:port 8080})))

(defstate server :start (start-aleph)
                 :stop (.close server))

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
