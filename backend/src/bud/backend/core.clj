(ns bud.backend.core
  (:require [clojure.data.json :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [jobryant.util :as u]
            [jobryant.txauth :as txauth]
            [bud.backend.config :as c]
            [bud.backend.query :as q]
            [bud.backend.tx]
            [compojure.core :refer [defroutes GET POST]]
            [datomic.client.api :as d]
            [datomic.ion.lambda.api-gateway :refer [ionize]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.format-params :refer [wrap-clojure-params]]
            [ring.middleware.cors :refer [wrap-cors]])
  (:import com.google.firebase.auth.FirebaseAuth))

(defn verify-token [token]
  (try
    (-> (FirebaseAuth/getInstance)
        (.verifyIdToken token)
        (.getClaims)
        (->> (into {})))
    (catch Exception e nil)))

(defn wrap-uid [handler]
  (fn [{:keys [request-method] :as req}]
    (if (= :options request-method)
      (handler req)
      (if-some [claims (some-> req
                               (get-in [:headers "authorization"])
                               (subs 7)
                               verify-token)]
        (handler (assoc req :claims claims :uid (get claims "user_id")))
        {:status 401
         :headers {"Content-Type" "text/plain"}
         :body "Invalid authentication token."}))))

(defn db []
  (d/db (c/conn)))

(defn init [{:keys [claims uid] :as req}]
  (let [tx [{:user/uid uid
             :user/email (claims "email")
             :user/emailVerified (claims "email_verified")}]
        {:keys [db-after] :as result} (c/transact (c/conn) {:tx-data tx})]
  {:headers {"Content-Type" "application/edn"}
   :body (pr-str (q/datoms-for db-after uid))}))

(defroutes routes
  (GET "/init" req (init req))
  (POST "/tx" req (txauth/handler
                    (assoc req
                           :conn (c/conn)
                           :transact-fn c/transact
                           :tx-fn :bud.backend.tx/authorize))))

(defn wrap-capture [handler]
  (fn [req]
    (println "capturin'" (:uri req))
    (u/capture req)
    (handler req)))

(def handler' (-> routes
                  wrap-uid
                  wrap-clojure-params
                  (wrap-defaults api-defaults)
                  (wrap-cors
                    :access-control-allow-origin [#"http://dev.impl.sh:8000" #"https://impl.sh"]
                    :access-control-allow-methods [:get :post]
                    :access-control-allow-headers ["Authorization" "Content-Type"])
                  wrap-capture))

(def handler (ionize handler'))
