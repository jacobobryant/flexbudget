(ns bud.client.event
  (:require [bud.client.db :as db :refer [conn]]
            [datascript.core :as d]
            [trident.datascript :as td]
            [trident.util :as u]
            [trident.cljs-http :refer [default-request]]
            [bud.client.config :as c]
            [cljs.reader :refer [read-string]]
            [cljs-time.core :refer [today plus years]]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(let [request* (default-request {:readers {'trident/eid #(tagged-literal 'trident/eid %)}})]
  (defn request
    ([method url payload]
     (go (<! (request* (merge {:method method
                               :url (str c/backend-host url)
                               :with-credentials? false
                               :oauth-token (u/js<! (db/token))}
                              payload)))))
    ([method url] (request method url {}))))

(defn persist [tx]
  (go (:body (<! (request :post "/tx" {:edn-params {:tx tx}})))))

(def transact! (partial td/transact! persist conn))

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
    (go (let [{datoms :body :as response} (<! (request :get "/init"))]
          (td/init-from-datomic! conn datoms)
          (when (not (some? @db/entry))
            (draft-entry!))
          (when (not (some? @db/goal))
            (init-goal!))
          (reset! db/loading? false)))))

(defn sign-out! []
  (.then (.. js/firebase auth signOut)
         #(set! (.. js/window -location -href) "/")))
