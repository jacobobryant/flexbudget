(ns user
  (:require [clojure.tools.namespace.repl :as tn]
            [nrepl.server :refer [start-server]]
            [orchestra.spec.test :as st]
            [bud.client.static :as static]))

(st/instrument)

(comment
  (nrepl.server/start-server :port 7988)

  ; for un-botching the repl
  (require '[clojure.tools.namespace.repl :as tn])
  (tn/refresh)

)

(defn nrepl []
  (start-server :port 7988))

(defn gensite []
  (static/gensite "target/dev"))
