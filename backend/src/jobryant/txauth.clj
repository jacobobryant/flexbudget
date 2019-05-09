(ns jobryant.txauth
  (:require [clojure.spec.alpha :as s]
            [datomic.client.api :as d]
            [datomic.ion.cast :as log]
            [orchestra.core :refer [defn-spec]]
            [jobryant.util :as u]))

; todo check backrefs for entities that change spec
; maybe... actually if an entity can change spec, it can probably be assumed that
; any references to it are valid with either spec

; move these to jobryant.datomic
(defn exists?
  ([db eid]
   (not-empty (d/q '[:find ?e :in $ ?e :where [?e]] db eid)))
  ([db attr value]
   (not-empty (d/q '[:find ?e :in $ ?a ?v :where [?e ?a ?v]] db attr value))))

(defn-spec ent-valid? boolean?
  [db any? spec any? ent map?]
  (and (s/valid? spec ent)
       (u/for-every? [[k vs] ent
                      v (u/wrap-vec vs)
                      :when (and (map? v)
                                 (contains? (s/registry) k))]
         (let [ent (if (empty? (dissoc v :db/id :db/ident))
                     (d/pull db '[*] (:db/id v))
                     v)]
           (s/valid? k ent)))))

(defn authorize [authorizers with-fn db uid tx]
  (let [{:keys [tx-data db-before db-after] :as result} (with-fn db {:tx-data tx})]
    (doseq [[e datoms] (group-by :e (rest tx-data))]
      (let [[before after :as ents]
            (map #(when (exists? % e) (d/pull % '[*] e)) [db-before db-after])

            auth-arg {:uid uid
                      :db-before db-before
                      :db-after db-after
                      :datoms datoms
                      :before before
                      :after after
                      :eid e}

            matching-authorizers
            (filter (fn [[specs _]]
                      (u/for-every? [[spec ent db]
                                     (map vector specs ents [db-before db-after])]
                          (and (= (some? spec) (some? ent))
                               (or (nil? spec) (ent-valid? db spec ent)))))
                    authorizers)

            authorized?
            (u/for-some? [[_ authorize-fn] matching-authorizers]
              (authorize-fn auth-arg))]

        (when (not authorized?)
          (u/capture auth-arg authorizers)
          (throw (ex-info "Entity change not authorized"
                          {:auth-arg auth-arg
                           :matches matching-authorizers})))))
    tx))

(defn handler [{:keys [allowed transact-fn conn auth-fn params uid]
                :or {allowed #{}} :as req}]
  (u/capture req)
  (let [tx (:tx params)]
    (if-some [bad-fn (some #(and (symbol? %) (not (contains? allowed %)))
                           (map first tx))]
      (do
        (log/alert {:msg "tx not allowed"
                    :bad-fn bad-fn
                    :uid uid
                    :tx tx})
        {:status 403
         :body (str "tx fn not allowed: " bad-fn)})
      (try
        {:headers {"Content-type" "application/edn"}
         :body (->> (transact-fn conn {:tx-data [[auth-fn uid tx]]})
                    :tempids
                    (map (fn [[k v]] [k (tagged-literal 'eid (str v))]))
                    (into {})
                    pr-str)}
        (catch Exception e
          (do
            (log/alert {:msg "Unhandled exception in tx"
                        :ex e
                        :uid uid
                        :tx tx})
            {:status 403
             :body "tx not allowed"}))))))
