(ns user
  (:require [bud.backend.core :as core]
            [trident.ion-dev.repl :as repl]
            [mount.core :as mount]
            [trident.repl :as tr]))

(repl/defhandler dev-lambda {:handler core/handler*})

(defmacro refresh []
  `(do (mount/stop)
       (tr/refresh :after 'mount.core/start)))

(defn init []
  (tr/init)
  (refresh))
