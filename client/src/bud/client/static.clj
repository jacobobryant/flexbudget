(ns bud.client.static
  (:require [jobryant.views.hiccup :as hiccup]
            [jobryant.views.re-com :as rc]
            [jobryant.util :as u]
            [bud.client.color :as color]
            [bud.client.shared :refer [navbar]]
            [clojure.string :refer [ends-with?]]
            [clojure.java.io :refer [make-parents]]
            [clojure.java.shell :refer [sh]]))

(def bootstrap-3
  [:link {:rel "stylesheet"
          :href "https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.5/css/bootstrap.min.css"}])

(def bootstrap-4
  [:link {:rel "stylesheet"
          :href "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
          :integrity "sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T"
          :crossorigin "anonymous"}])

(def bootstrap-jquery
  (list
    [:script {:src "https://code.jquery.com/jquery-3.3.1.slim.min.js"
              :integrity "sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo"
              :crossorigin "anonymous"}]
    [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js"
              :integrity "sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q"
              :crossorigin "anonymous"}]
    [:script {:src "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"
              :integrity "sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM"
              :crossorigin "anonymous"}]
    [:script {:src "/js/form.js"}]))

(def firebase
  (list [:script {:src "https://www.gstatic.com/firebasejs/5.10.1/firebase-app.js"}]
        [:script {:src "https://www.gstatic.com/firebasejs/5.10.1/firebase-auth.js"}]
        [:script {:src "/js/firebase-init.js"}]))

(def ensure-logged-in
  [:script "firebase.auth().onAuthStateChanged(u => { if (!u) window.location.href = '/'; });"])
  ;[:script "if (firebase.auth().currentUser == null) window.location.href = \"/\";"])

(def ensure-logged-out
  [:script "firebase.auth().onAuthStateChanged(u => { if (u) window.location.href = '/app/'; });"])
  ;[:script "if (firebase.auth().currentUser != null) window.location.href = \"/app.html\";"])

(def firebase-ui
  (list [:script {:src "https://cdn.firebase.com/libs/firebaseui/3.6.0/firebaseui.js"}]
        [:link {:type "text/css" :rel "stylesheet"
                :href "https://cdn.firebase.com/libs/firebaseui/3.6.0/firebaseui.css"}]
        [:script {:src "/js/firebase-signin.js"}]))

(def re-com
  (list [:link {:rel "stylesheet" :href "/css/material-design-iconic-font.min.css"}]
        [:link {:rel "stylesheet" :href "/css/re-com.css"}]
        [:link {:rel "stylesheet" :href "/css/main.css"}]

        [:link {:href "https://fonts.googleapis.com/css?family=Roboto:300,400,500,700,400italic"
                :rel "stylesheet" :type "text/css"}]
        [:link {:href "https://fonts.googleapis.com/css?family=Roboto+Condensed:400,300"
                :rel "stylesheet" :type "text/css"}]))

(defn head [& items]
  (into [:head [:meta {:charset "utf-8"}]] items))

(defn html [& contents]
  (hiccup/html (into [:html {:lang "en"}] contents)))

(def landing
  (html
    (head
      [:title "FlexBudget"]
      firebase ensure-logged-out bootstrap-4 firebase-ui)
    [:body
      (navbar)
      (rc/gap "30px")
      [:div.container
       [:div.row
        [:div.col-lg-6
         (rc/p "FlexBudget is a simple tool that gives you a
               high-level understanding of your spending habits
               without forcing you to fill out a detailed budget.")
         (rc/p
           "See the video for a short demonstration.")
         [:div#firebaseui-auth-container]]
        [:div.col-lg-6
         [:iframe {:width "560"
                   :height "315"
                   :style {:border-width "1px"
                           :border-style "solid"
                           :border-color "black"}
                   :src "https://www.youtube.com/embed/e5z0brseEV0"
                   :frameborder "0"
                   :allow "accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"
                   :allowfullscreen true}]]]]]))

(def app
  (html
    (head
      [:title "FlexBudget"]
      firebase ensure-logged-in bootstrap-3 re-com
      [:script {:src "/cljs/main.js" :type "text/javascript"}])
    [:body {:style {:background color/background}}
     [:div#app {:style {:height "inherit"}}]
     [:script "window.onload = function () { bud.client.core._main(); }"]]))

(def routes {"/" landing
             "/app/" app})

(defn gensite [root]
  (sh "rsync" "-a" "--delete" "--exclude" "cljs" "assets/" root)
  (doseq [[path contents] routes]
    (let [path (str root path (when (ends-with? path "/") "index.html"))]
      (make-parents path)
      (spit path contents))))
