(ns bud.client.event
  (:require [bud.client.db :as db :refer [conn]]
            [jobryant.datascript.core :as d]
            [cljs-time.core :refer [today plus years]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn persist [tx]
  (let [payload {:edn-params {:tx tx}
                 :headers {"X-CSRF-Token" @db/anti-forgery-token}}
        response (http/post "/tx" payload)]
    (go (:body (<! response)))))

(def transact! (partial d/transact! persist conn))

(defn draft-entry! []
  (transact! [{:db/id "tmp"
               :auth/owner [:user/email @db/email]
               :entry/draft true}]))

(defn init-goal! []
  (transact! [{:db/id "tmp"
               :auth/owner [:user/email @db/email]
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
               :auth/owner [:user/email @db/email]
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
  (go (let [response (<! (http/get "/init"))
            {:keys [datoms email]} (:body response)]
        (d/init-from-datomic! conn datoms)
        (reset! db/anti-forgery-token (get-in response [:headers "__anti-forgery"]))
        (reset! db/email email)
        (when (not (some? @db/entry))
          (draft-entry!))
        (when (not (some? @db/goal))
          (init-goal!))
        (reset! db/loading? false))))

(defonce _ (init!))
