(ns bud.backend.core
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [jobryant.util :as u]
            [bud.backend.config :as c]
            [compojure.core :refer [defroutes GET POST]]
            [datomic.client.api :as d]
            [datomic.ion.lambda.api-gateway :refer [ionize]]))

(def client
  (memoize #(c/client-fn c/client-cfg)))

(def conn
  (memoize #(let [args [(client) {:db-name c/db-name}]]
              (apply d/create-database args)
              (apply d/connect args))))

(defn init [req]
  (u/pprint req)
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "hello"})

(defroutes routes
  (GET "/init" req (init req))
  ;(POST "/tx" req (tx req)))
  )

(def handler' routes)
(def handler (ionize handler'))
