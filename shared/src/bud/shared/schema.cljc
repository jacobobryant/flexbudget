(ns bud.shared.schema
  (:require [clojure.spec.alpha :as s]
            [jobryant.util :as u]))

(def schema
  {:user/email [:db.type/string :db.unique/identity]
   :user/password [:db.type/string]

   :auth/owner [:db.type/ref]

   :entry/date [:db.type/instant]
   :entry/draft [:db.type/boolean]
   :entry/asset [:db.type/ref :db/isComponent :db.cardinality/many]
   :entry/delta [:db.type/ref :db/isComponent :db.cardinality/many]

   :misc/description [:db.type/string]
   :misc/amount [:db.type/long]

   :delta/frequency [:db.type/keyword]
   :delta/basis [:db.type/instant]

   :goal/date [:db.type/instant]
   :goal/allowance [:db.type/long]})

(s/def ::password #(and (string? %) (<= (count %) 100)))
(s/def ::email ::password)
(s/def ::goal (u/only-keys [:auth/owner :goal/allowance :goal/date :misc/amount]))
(s/def ::delta (u/only-keys [:auth/owner :delta/frequency :misc/amount :misc/description]
                            [:delta/basis]))
(s/def :entry/asset (u/only-keys [:misc/amount :misc/description]))
(s/def ::entry.draft (u/only-keys [:auth/owner :entry/draft] [:entry/asset]))
(s/def ::entry (u/only-keys [:auth/owner :entry/date] [:entry/asset]))
