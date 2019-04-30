(ns user
  (:require [clojure.tools.namespace.repl :as tn]
            [nrepl.server :refer [start-server]]
            [orchestra.spec.test :as st]))

(st/instrument)

(def refresh-dirs ["src" "env/dev"])

(comment
  (nrepl.server/start-server :port 7888)

  ; for un-botching the repl
  (require '[clojure.tools.namespace.repl :as tn])
  (apply tn/set-refresh-dirs refresh-dirs)
  (tn/refresh)

)

(apply tn/set-refresh-dirs refresh-dirs)

(defn nrepl []
  (start-server :port 7888))
