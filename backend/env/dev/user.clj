(ns user
  (:require [clojure.tools.namespace.repl :as tn]
            [mount.core :as mount :refer [defstate]]
            [nrepl.server :refer [start-server]]
            [orchestra.spec.test :as st]
            [bud.backend.core :as core]
            [compute.datomic-client-memdb.core]
            [immutant.web :as imm]
            [datomic.ion.cast :refer [initialize-redirect]]))

(comment
  (nrepl.server/start-server :port 7888)

  ; for un-botching the repl
  (require '[clojure.tools.namespace.repl :as tn])
  (tn/refresh)

)

(initialize-redirect :stdout)

(st/instrument)

(defn start-immutant []
  (imm/run
    core/handler'
    {:port 8080 }))

(defstate server :start (start-immutant)
                 :stop (imm/stop))

(defn nrepl []
  (start-server :port 7888))

(defn go []
  (mount/start)
  (println :ready))

(defmacro reset []
  `(do (mount/stop)
       (tn/refresh :after 'user/go)
       (use 'clojure.repl)))
