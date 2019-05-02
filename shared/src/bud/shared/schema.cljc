(ns bud.shared.schema
  (:require [clojure.spec.alpha :as s]
            [jobryant.util :as u]))

(def schema
  {:user/uid [:db.type/string :db.unique/identity]
   :user/email [:db.type/string :db.unique/identity]
   :user/emailVerified [:db.type/boolean]

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

(def datomic-schema (u/datomic-schema schema))
(def ds-schema (u/datascript-schema schema))

(s/def ::goal (u/ent-spec [:auth/owner :goal/allowance :goal/date :misc/amount]))
(s/def ::delta (u/ent-spec [:auth/owner :delta/frequency :misc/amount :misc/description]
                            [:delta/basis]))
(s/def :entry/asset (u/ent-spec [:misc/amount :misc/description]))
(s/def ::entry.draft (u/ent-spec [:auth/owner :entry/draft] [:entry/asset]))
(s/def ::entry (u/ent-spec [:auth/owner :entry/date] [:entry/asset]))
