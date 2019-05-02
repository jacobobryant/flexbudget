(ns user
  (:require [clojure.tools.namespace.repl :as tn]
            [mount.core :as mount :refer [defstate]]
            [nrepl.server :refer [start-server]]
            [orchestra.spec.test :as st]
            [bud.backend.core :as core]
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
  (aleph/start-server
    core/handler'
    {:port 8080}))

(defstate server :start (start-aleph)
                 :stop (.close server))

(tn/set-refresh-dirs "src")

(defn nrepl []
  (start-server :port 7888))

(defn go []
  (mount/start)
  (println :ready))

(defmacro reset []
  `(do (mount/stop)
       (tn/refresh :after 'user/go)
       (use 'clojure.repl)))
