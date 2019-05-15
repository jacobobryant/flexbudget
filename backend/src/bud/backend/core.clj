(ns bud.backend.core
  (:require [jobryant.util :as u]
            [jobryant.datomic-cloud.txauth :as txauth]
            [jobryant.firebase :refer [verify-token]]
            [jobryant.ion :refer [set-timbre-ion-appender!]]
            [jobryant.trident :as trident]
            [bud.backend.env :refer [conn config get-param]]
            [bud.backend.query :as q]
            [bud.backend.tx]
            [datomic.client.api :as d]
            [compojure.core :refer [defroutes GET POST]]
            [datomic.ion.lambda.api-gateway :refer [ionize]]))

(trident/init!)

(defn init [{:keys [claims uid] :as req}]
  (let [tx [{:user/uid uid
             :user/email (claims "email")
             :user/emailVerified (claims "email_verified")}]
        {:keys [db-after] :as result} (d/transact conn {:tx-data tx})
        datoms (pr-str (q/datoms-for db-after uid))]
    {:headers {"Content-Type" "application/edn"}
     :body datoms}))

(defroutes routes
  (GET "/init" req (init req))
  (POST "/tx" req (txauth/handler
                    (merge req
                           (select-keys config [:local-tx-fns?])
                           {:conn conn
                            :auth-fn 'bud.backend.tx/authorize}))))

(def trident-config
  {:origins [#"http://dev.notjust.us:8000"
             #"https://notjust.us"
             #"https://www.notjust.us"]
   :uid-opts {:verify-token
              (fn [token] (verify-token token #(get-param :firebase-key)))}
   :state-var #'conn})

(trident/defhandlers routes trident-config)
