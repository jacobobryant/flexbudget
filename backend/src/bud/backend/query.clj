(ns bud.backend.query
  (:require [datomic.client.api :as d]))

(defn datoms-for [db uid]
  (let [user-eid (:db/id (d/pull db [:db/id] [:user/uid uid]))]
    (->>
      (conj
        (vec (d/q '[:find ?e ?attr ?v :in $ ?user :where
                    [?ent :auth/owner ?user]
                    (or
                      [(identity ?ent) ?e]
                      [?ent :entry/asset ?e])
                    [?e ?a ?v]
                    [?a :db/ident ?attr]]
                  db user-eid))
        [user-eid :user/uid uid]))))
