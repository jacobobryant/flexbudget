(ns user
  (:require [trident.repl :as repl]
            [bud.client.static :as static]))

(defn gensite []
  (static/gensite "target/dev"))

(defmacro refresh []
  `(repl/refresh))

(defn init []
  (repl/init {:nrepl-port 7988})
  (gensite))
