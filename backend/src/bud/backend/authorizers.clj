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
     (owns? ?uid ?entry)]])

(defn owns? [{:keys [db-before db-after before after uid eid]}]
  (u/for-every? [[ent db] (map vector [before after] [db-before db-after])
                 :when (some? ent)]
    (not-empty
      (d/q '[:find ?uid :in $ % ?uid ?e :where
             (owns? ?uid ?e)]
           db rules uid eid))))

(def authorizers
  {[nil ::schema/entry.draft]
   (fn [{:keys [after db-after uid]}]
     (not-empty
       (d/q '[:find ?e :in $ ?e ?user :where
              [?e :auth/owner ?user]
              [?e :entry/draft true]
              (not-join [?user ?e]
                [?other :auth/owner ?user]
                [?other :entry/draft true]
                [(not= ?e ?other)])]
            db-after (:db/id after) [:user/uid uid])))
   [::schema/entry.draft ::schema/entry.draft] owns?

   [nil ::schema/goal]
   (fn [{:keys [after db-after uid]}]
     (not-empty
       (d/q '[:find ?e :in $ ?e ?user :where
              [?e :auth/owner ?user]
              [?e :goal/allowance]
              (not-join [?user ?e]
                [?other :auth/owner ?user]
                [?other :goal/allowance]
                [(not= ?e ?other)])]
            db-after (:db/id after) [:user/uid uid])))
   [::schema/goal ::schema/goal] owns?

   [nil :entry/asset] owns?
   [:entry/asset :entry/asset] owns?
   [:entry/asset nil] owns?

   [nil ::schema/delta] owns?
   [::schema/delta ::schema/delta] owns?
   [::schema/delta nil] owns?})
