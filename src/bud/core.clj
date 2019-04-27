(ns bud.core
  (:require [bud.backend.views :as views]
            [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [datomic.client.api :as d]
            [datomic.ion.lambda.api-gateway :refer [ionize]]))


(def landing (ionize (fn [_] {:status 200
                              :headers {"Content-Type" "text/html"}
                              :body (views/landing)})))
