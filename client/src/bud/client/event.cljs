(ns bud.client.event
  (:require [bud.client.db :as db :refer [conn]]
            [jobryant.datascript.core :as d]
            [jobryant.util :as u]
            [bud.shared.config :as c]
            [cljs-time.core :refer [today plus years]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<! put! chan close!]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [bud.client.event :refer [request]]))

(defn persist [tx]
  (go (:body (request http/post "/tx" {:edn-params {:tx tx}}))))

(def transact! (partial d/transact! persist conn))

(defn draft-entry! []
  (transact! [{:db/id "tmp"
               :auth/owner [:user/uid (db/uid)]
               :entry/draft true}]))

(defn init-goal! []
  (transact! [{:db/id "tmp"
               :auth/owner [:user/uid (db/uid)]
               :misc/amount 0
               :goal/allowance 0
               :goal/date (plus (today) (years 1))}]))

(defn asset! []
  (transact! [{:db/id (:db/id @db/entry)
               :entry/asset [{:db/id "tmp"
                              :misc/amount 0
                              :misc/description ""}]}]))

(defn delta! []
  (transact! [{:db/id "tmp"
               :auth/owner [:user/uid (db/uid)]
               :misc/amount 0
               :misc/description ""
               :delta/frequency :monthly}]))

(defn rm! [id]
  (transact! [[:db.fn/retractEntity id]]))

(defn save! [ent]
  (transact! [(->> ent
                   (remove (comp map? second))
                   (into {}))]))

(defn init! []
  (go (let [{datoms :body :as response} (request http/get "/init")]
        (u/pprint response)
        (d/init-from-datomic! conn datoms)
        (when (not (some? @db/entry))
          (draft-entry!))
        (when (not (some? @db/goal))
          (init-goal!))
        (reset! db/loading? false))))

(defonce _ (init!))
