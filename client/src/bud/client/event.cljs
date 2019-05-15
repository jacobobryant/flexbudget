(ns bud.client.event
  (:require [bud.client.db :as db :refer [conn]]
            [jobryant.datascript.core :as d]
            [jobryant.util :as u]
            [bud.client.config :as c]
            [cljs.reader :refer [read-string]]
            [cljs-time.core :refer [today plus years]]
            [cljs-http.client :as http]
            [cljs.core.async])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [bud.client.event :refer [request]]))

(let [edn-opts {:readers {'eid #(tagged-literal 'eid %)}}]
  (defn request* [method uri payload token]
    (with-redefs [read-string (partial read-string edn-opts)]
      (method (str c/backend-host uri)
              (merge {:with-credentials? false
                      :oauth-token token}
                     payload)))))

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
                              :misc/description ""
                              :misc/order (inc @db/max-asset-order)}]}]))

(defn delta! []
  (transact! [{:db/id "tmp"
               :auth/owner [:user/uid (db/uid)]
               :misc/amount 0
               :misc/description ""
               :misc/order (inc @db/max-delta-order)
               :delta/frequency :monthly}]))

(defn rm! [id]
  (transact! [[:db/retractEntity id]]))

(defn save! [ent]
  (transact! [(->> ent
                   (remove (comp map? second))
                   (into {}))]))

(defn init! []
  (when @db/loading?
    (go (let [{datoms :body :as response} (request http/get "/init")]
          (d/init-from-datomic! conn datoms)
          (when (not (some? @db/entry))
            (draft-entry!))
          (when (not (some? @db/goal))
            (init-goal!))
          (reset! db/loading? false)))))

(defn sign-out! []
  (.then (.. js/firebase auth signOut)
         #(set! (.. js/window -location -href) "/")))
