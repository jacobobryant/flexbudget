(ns bud.backend.core
  (:require [trident.web :as web]
            [trident.util] ; for data_readers
            [bud.backend.authorizers :refer [authorizers]]
            [bud.backend.query :refer [datoms-for]]
            [bud.shared.schema :refer [schema]]))

(web/init!
  {:app-name "bud"
   :origins [#"http://dev.notjust.us:8000"
             #"https://notjust.us"
             #"https://www.notjust.us"]
   :authorizers `authorizers
   :datoms-for datoms-for
   :schema schema})
