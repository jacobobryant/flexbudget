(ns bud.backend.core
  (:require [jobryant.trident :as trident]
            [bud.backend.authorizers :refer [authorizers]]
            [bud.backend.query :refer [datoms-for]]
            [bud.shared.schema :refer [schema]]))

(def default-config
  {:app-name "bud"
   :origins [#"http://dev.notjust.us:8000"
             #"https://notjust.us"
             #"https://www.notjust.us"]
   :client-cfg {:system "bud"
                :endpoint "http://entry.bud.us-east-1.datomic.net:8182/"}
   :authorizers `authorizers
   :datoms-for datoms-for
   :schema schema})

(defonce _ (trident/init! default-config))
