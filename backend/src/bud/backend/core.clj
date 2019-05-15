(ns bud.backend.core
  (:require [jobryant.trident :as trident]
            [bud.backend.authorizers :refer [authorizers]]
            [bud.backend.query :refer [datoms-for]]
            [bud.shared.schema :refer [schema]]))

(trident/init!
  {:app-name "bud"
   :origins [#"http://dev.notjust.us:8000"
             #"https://notjust.us"
             #"https://www.notjust.us"]
   :authorizers `authorizers
   :datoms-for datoms-for
   :schema schema})
