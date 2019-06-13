(ns user
  (:require [trident.repl :as repl]
            [trident.util]
            [mount.core :as mount]))

(defmacro refresh []
  `(do
     (mount/stop)
     (repl/refresh [:after 'mount.core/start])))

(defn init []
  (repl/init {:refresh-args [:after 'mount.core/start]}))
