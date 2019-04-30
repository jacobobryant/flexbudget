(ns bud.client.db
  (:require [reagent.core :as r]
            [reagent.ratom :refer-macros [reaction]]
            [jobryant.util :as u]
            [clojure.pprint :refer [pprint]]
            [jobryant.datascript.core :as d]
            [jobryant.datomic.util :as du]
            [cljs-time.core :refer [before? today]]
            [bud.client.calc :as calc]
            [bud.shared.schema :refer [schema]])
  (:require-macros [jobryant.datascript.core :refer [defq]]))

(defonce conn (d/create-conn (du/datascript-schema schema)))
(defonce email (r/atom nil))
(defonce loading? (r/atom true))
(defonce anti-forgery-token (r/atom nil))

(defq entries
  (->> (d/db conn)
       (d/q '[:find [(pull ?e [*]) ...] :where
              (or [?e :entry/draft]
                  [?e :entry/data])])
       (sort-by :db/id)))

(defq deltas
  (->> (d/db conn)
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
