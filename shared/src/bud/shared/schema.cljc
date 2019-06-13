(ns bud.shared.schema
  (:require [clojure.spec.alpha :as s]
            [trident.util.datomic :as ud]))

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
   :misc/order [:db.type/long]

   :delta/frequency [:db.type/keyword]
   :delta/basis [:db.type/instant]

   :goal/date [:db.type/instant]
   :goal/allowance [:db.type/long]})

(s/def ::goal (ud/ent-keys [:auth/owner :goal/allowance :goal/date :misc/amount]))
(s/def ::delta (ud/ent-keys [:auth/owner :delta/frequency :misc/amount :misc/description]
                            [:delta/basis :misc/order]))
(s/def :entry/asset (ud/ent-keys [:misc/amount :misc/description] [:misc/order]))
(s/def ::entry.draft (ud/ent-keys [:auth/owner :entry/draft] [:entry/asset]))
(s/def ::entry (ud/ent-keys [:auth/owner :entry/date] [:entry/asset]))
