(ns bud.backend.authorizers
  (:require [bud.shared.schema :as schema]
            [trident.util :as u]
            [datomic.client.api :as d]))

(def rules
  '[[(owns? [?uid ?e])
     [?e :auth/owner ?user]
     [?user :user/uid ?uid]]
    [(owns? [?uid ?asset])
     [?entry :entry/asset ?asset]
     (owns? ?uid ?entry)]
    [(only-one? [?e ?a ?uid])
     (owns? ?uid ?e)
     [?e ?a]
     (not-join [?e ?uid]
       (owns? ?uid ?other)
       [?e ?a]
       [(not= ?e ?other)])]])

(defn owns? [{:keys [db-before db-after before after uid eid]}]
  (u/for-every? [[ent db] [[before db-before] [after db-after]]
                 :when (some? ent)]
    (not-empty
      (d/q '[:find ?uid :in $ % ?uid ?e :where
             (owns? ?uid ?e)]
           db rules uid eid))))

(defn only-one? [attr {:keys [after db-after uid]}]
  (not-empty
    (d/q '[:find ?e :in $ % ?e ?a ?uid :where
           (only-one? ?e ?a ?uid)]
         db-after (:db/id after) attr uid)))

(def authorizers
  {[nil ::schema/entry.draft] (partial only-one? :entry/draft)
   [::schema/entry.draft ::schema/entry.draft] owns?

   [nil ::schema/goal] (partial only-one? :goal/allowance)
   [::schema/goal ::schema/goal] owns?

   [nil :entry/asset] owns?
   [:entry/asset :entry/asset] owns?
   [:entry/asset nil] owns?

   [nil ::schema/delta] owns?
   [::schema/delta ::schema/delta] owns?
   [::schema/delta nil] owns?})
