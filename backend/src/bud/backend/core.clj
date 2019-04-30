(ns bud.core
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [jobryant.util :as u]
            [datomic.client.api :as d]
            [datomic.ion.lambda.api-gateway :refer [ionize]]))

(defn init [req]
  (u/capture req)
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "hello"})
(def init (ionize init))
