(ns jobryant.util
  (:require [jobryant.util.core :as u]
            [jobryant.util.datomic]
    #?(:clj [potemkin :refer [import-vars]]))
  #?(:cljs (:require-macros [jobryant.util])))

#?(:clj (u/pullall jobryant.util.core jobryant.util.datomic))

#?(:cljs (do

; see if we can restrict this to fns that are only defined for cljs
(u/cljs-pullall jobryant.util.datomic
                datascript-schema
                datomic-schema
                ent-spec
                expand-flags
                tempify-datoms
                translate-eids
                wrap-vec)

(u/cljs-pullall jobryant.util.core
                assoc-some
                c+
                c-
                conj-some
                cop
                deep-merge
                deep-merge-some
                dissoc-by
                format
                indexed
                instant?
                map-from
                map-inverse
                merge-some
                ord
                parse-int
                pprint
                pred->
                rand-str
                remove-nil-empty
                remove-nils
                split-by
                to-chan
                zip)

))
