(ns bud.backend.core
  (:require [jobryant.util :as u]
            [jobryant.txauth :as txauth]
            [bud.backend.env :refer [conn transact config get-param]]
            [bud.backend.query :as q]
            [bud.backend.tx]
            [compojure.core :refer [defroutes GET POST]]
            [datomic.client.api :as d]
            [datomic.ion.lambda.api-gateway :refer [ionize]]
            [datomic.ion.cast :as log]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.format-params :refer [wrap-clojure-params]]
            [ring.middleware.cors :refer [wrap-cors]]
            [clojure.java.io :refer [input-stream]]
            [mount.core :as mount])
  (:import com.google.firebase.auth.FirebaseAuth
           [com.google.firebase FirebaseApp FirebaseOptions$Builder]
           com.google.auth.oauth2.GoogleCredentials))

(defn init-firebase! []
  (let [credentials (-> :firebase-key
                        get-param
                        (.getBytes)
                        input-stream
                        (GoogleCredentials/fromStream))
        options (-> (new FirebaseOptions$Builder)
                    (.setCredentials credentials)
                    (.setDatabaseUrl "https://budget-6fc5c.firebaseio.com")
                    .build)]
    (FirebaseApp/initializeApp options)))

(defn verify-token [token]
  (when (= 0 (count (FirebaseApp/getApps)))
    (init-firebase!))
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

(defn init [{:keys [claims uid] :as req}]
  (let [tx [{:user/uid uid
             :user/email (claims "email")
             :user/emailVerified (claims "email_verified")}]
        {:keys [db-after] :as result} (transact conn {:tx-data tx})
        datoms (pr-str (sort (q/datoms-for db-after uid)))]
    {:headers {"Content-Type" "application/edn"}
     :body datoms}))

(defroutes routes
  (GET "/init" req (init req))
  (POST "/tx" req (txauth/handler
                    (merge req
                           (select-keys config [:local-tx-fns?])
                           {:conn conn
                            :transact-fn transact
                            :auth-fn 'bud.backend.tx/authorize}))))

(defn wrap-capture [handler]
  (fn [req]
    (log/event {:msg "got request"
                :uri (:uri req)
                :conn-type (type conn)
                :should-start (contains? #{mount.core.NotStartedState mount.core.DerefableState} (type conn))})
    (when (contains? #{mount.core.NotStartedState mount.core.DerefableState} (type conn))
      (log/event {:msg "Started mount" :result (mount/start)}))
    (let [result (try (handler req)
                      (catch Exception e
                        (log/alert {:msg "Unhandled exception in handler" :ex e})
                        (u/pprint e)
                        (println)
                        {:status 500}))]
      (when (not= 200 (:status result))
        (u/capture req)
        (u/pprint req)
        (println)
        (u/pprint result)
        (println)
        (println (:uri req) "failed"))
      result)))

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
