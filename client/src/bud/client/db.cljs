(ns bud.client.db
  (:require [reagent.core :as r]
            [reagent.ratom :refer-macros [reaction]]
            [jobryant.util :as u]
            [clojure.pprint :refer [pprint]]
            [jobryant.datascript.core :as d]
            [cljs-time.core :refer [before? today]]
            [bud.client.calc :as calc]
            [bud.shared.schema :refer [schema]])
  (:require-macros [jobryant.datascript.core :refer [defq]]))

(def user #(.. js/firebase auth -currentUser))
(def token #(.getIdToken (user)))
(def uid #(.-uid (user)))
(def email #(.-email (user)))

(defonce conn (d/create-conn (u/datascript-schema schema)))
(defonce loading? (r/atom true))

(defq entries
  (->> @conn
       (d/q '[:find [(pull ?e [*]) ...] :where
              (or [?e :entry/draft]
                  [?e :entry/data])])
       (sort-by :db/id)))

(defq deltas
  (->> @conn
       (d/q '[:find [(pull ?e [*]) ...] :where
              [?e :delta/frequency]])
       (sort-by :db/id)))

(defq goal
  (d/q '[:find (pull ?goal [*]) . :where
         [?goal :goal/allowance]]
     (d/db conn)))

(def entry (reaction (last @entries)))
(def draft? (reaction (:entry/draft @entry)))
(def assets (reaction (sort-by :db/id (:entry/asset @entry))))
(def goal-complete? (reaction (and (every? #(contains? @goal %) [:misc/amount :goal/date])
                                   (before? (today) (:goal/date @goal)))))
(def net-assets (reaction (->> @assets (map :misc/amount) (reduce +))))
(def forecasted-total (reaction (->> @deltas
                                     (filter #(contains? % :delta/basis))
                                     (map #(calc/forecast (:goal/date @goal) %))
                                     (reduce + @net-assets))))
(def weekly-allowance (reaction (calc/weekly-allowance @forecasted-total @goal)))
(def surplus (reaction (calc/surplus @forecasted-total @goal)))
