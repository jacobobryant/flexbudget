(ns jobryant.txauth
  (:require [clojure.spec.alpha :as s]
            [datomic.client.api :as d]
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

(defn ent-valid? [db spec ent]
  (and (s/valid? spec ent)
       (u/for-every? [[k vs] ent
                      v (u/wrap-vec vs)
                      :when (map? v)]
         (let [ent (if (= 1 (count (keys v)))
                     (d/pull db '[*] (:db/id v))
                     v)]
           (s/valid? k ent)))))

(defn authorize [authorizers db uid tx]
  (let [{:keys [tx-data db-before db-after] :as result} (d/with db tx)]
    (doseq [[e datoms] (group-by :e (rest tx-data))]
      (let [[before after :as ents]
            (map #(when (exists? % e) (d/pull % '[*] e)) [db-before db-after])

            authorized?
            (u/for-some? [[specs authorize-fn] authorizers]
              (let [matches-specs?
                    (u/for-every? [[spec ent db]
                                   (map vector specs ents [db-before db-after])]
                      (and (= (some? spec) (some? ent))
                           (or (nil? spec) (ent-valid? db spec ent))))]
                (and matches-specs? (authorize-fn
                                      {:uid uid
                                       :db-before db-before
                                       :db-after db-after
                                       :datoms datoms
                                       :before before
                                       :after after
                                       :eid e}))))]

        (when (not authorized?)
          (throw (ex-info "Entity change not authorized"
                          {:before before
                           :after after})))))
    tx))

(defn handler [{:keys [tx-fn-blacklist transact-fn conn tx-fn params uid]
                :or {tx-fn-blacklist #{}}
                :as req}]
  (u/capture req)
  (let [tx (:tx params)]
    (if-some [bad-fn (some #(or (symbol? %) (contains? tx-fn-blacklist %))
                           (map first tx))]
      {:status 403
       :body (str "tx fn not allowed: " bad-fn)}
      (try
        {:headers {"Content-type" "application/edn"}
         :body (pr-str (:tempids (transact-fn conn {:tx-data [[tx-fn uid tx]]})))}
        (catch Exception e
          (do
            (u/pprint e)
            {:status 403}))))))
