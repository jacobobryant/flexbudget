(ns bud.backend.views
  (:require [jobryant.views :refer [defview form]]
            [jobryant.hiccup.core :refer [html]]
            [jobryant.re-com.core :as rc]
            [jobryant.util :as u]
            [bud.color :as color]
            [bud.views :refer [navbar]]))

(defn head [{:keys [bootstrap-version] :or {bootstrap-version 4}}]
  [:head
   [:meta {:charset "utf-8"}]

   ;[:link {:href "vendor/fontawesome-free/css/all.min.css" :rel "stylesheet"}]
   ;[:link {:href "vendor/simple-line-icons/css/simple-line-icons.css"
   ;        :rel "stylesheet" :type "text/css"}]
   ;[:link {:href "https://fonts.googleapis.com/css?family=Lato:300,400,700,300italic,400italic,700italic"
   ;        :rel "stylesheet" :type "text/css"}]

   ; Bootstrap
   (if (= bootstrap-version 3)
     [:link {:rel "stylesheet"
             :href "https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.5/css/bootstrap.min.css"}]
     [:link {:rel "stylesheet"
             :href "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
             :integrity "sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T"
             :crossorigin "anonymous"}])
   [:script {:src "https://code.jquery.com/jquery-3.3.1.slim.min.js"
             :integrity "sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo"
             :crossorigin "anonymous"}]
   [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js"
             :integrity "sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q"
             :crossorigin "anonymous"}]
   [:script {:src "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"
             :integrity "sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM"
             :crossorigin "anonymous"}]

   [:script {:src "/js/form.js"}]])

(defview body [{:keys [background head-items bootstrap-version]
                :or {background color/primary
                     head-items nil
                     bootstrap-version 4}
                :as opts}
               contents]
  (html
    [:html
     {:lang "en"}
     (into (head (select-keys opts [:bootstrap-version]))
           head-items)
     [:body {:style {:background-color background}}
      contents]]))

(defn card [{:keys [title alert]} & contents]
  [:div.container
   [:div.row
    [:div.col-sm-9.col-md-7.col-lg-5.mx-auto
     [:div.card.card-signin.my-5
      alert
      [:div.card-body
       [:h5.card-title.text-center title]
       contents]]]]])

(defn alert [alert-type text]
  [(keyword (format "div.alert.alert-%s.text-center" alert-type))
   {:role "alert"
    :style {:margin-bottom 0}}
   text])

(defn already-user [req]
  (let [email (get-in req [:params :already-user])
        already-user? (some? email)]
      (when already-user?
        (alert "info" "Email address already in use."))))

(defn login [req]
  (body
    (navbar {})
    (card {:title "Sign in to FlexBudget"
           :alert (or (already-user req)
                      (when (contains? (:params req) :invalid)
                        (alert "danger" "Login failed. Invalid username or password.")))}
          (form {:action "/login"}
                (rc/v-box
                  [:input.form-control
                   {:type "email"
                    :placeholder "Email address"
                    :name "email"
                    :required true
                    :value (get-in req [:params :already-user])
                    :autofocus true}]
                  [:input.form-control
                   {:type "password"
                    :placeholder "Password"
                    :name "password"
                    :required true}]
                  [:button.btn.btn-lg.btn-primary.btn-block
                   {:type "submit"}
                   "Sign In"]
                  [:a.btn.btn-lg.btn-secondary.btn-block
                   {:href "/register"}
                   "Register"])))))

(defn register [req]
  (body
    (navbar {})
    (card {:title "Sign up for FlexBudget"
           :alert (already-user req)}
          (form {:action "/finish-register"
                 :oninput "confirmPassword.setCustomValidity(password.value != confirmPassword.value ? \"Passwords do not match.\" : \"\")"}
                (rc/v-box
                  [:input.form-control
                   {:type "email"
                    :placeholder "Email address"
                    :name "email"
                    :required true
                    :value (get-in req [:params :email])
                    :autofocus true}]
                  [:input.form-control
                   {:type "password"
                    :placeholder "Password"
                    :name "password"
                    :required true}]
                  [:div.input-group
                   [:input.form-control
                    {:type "password"
                     :placeholder "Confirm password"
                     :name "confirmPassword"
                     :required true
                     :data-match "#password"}]
                   [:div.invalid-feedback "Passwords don't match."]]
                  [:button.btn.btn-lg.btn-primary.btn-block
                   {:type "submit"}
                   "Register"]
                  [:a.btn.btn-lg.btn-secondary.btn-block
                   {:href "/login"}
                   "Sign In"]
                  )))))

(defn landing []
  (body
    {:head-items [[:link {:href "css/landing-page.min.css" :rel "stylesheet"}]]
     :background "white"}
    [:div
     (navbar {:show-login? true})
     [:header.masthead.text-white.text-center
      [:div.overlay]
      [:div.container
       [:div.row
        [:div.col-xl-9.mx-auto
         [:h1.mb-5 "Budget flexibly yo"]]
        [:div.col-md-10.col-lg-8.col-xl-7.mx-auto
         (form {:action "/register"}
               [:div.form-row
                [:div.col-12.col-md-9.mb-2.mb-md-0
                 [:input.form-control.form-control-lg
                  {:type "email"
                   :name "email"
                   :placeholder "Enter your email..."}]]
                [:div.col-12.col-md-3
                 [:button.btn.btn-block.btn-lg.btn-primary
                  {:type "submit"}
                  "Sign up!"]]])]]]]]))

(defn app []
  (body
    {:head-items [[:link {:rel "stylesheet" :href "/assets/css/material-design-iconic-font.min.css"}]
                  [:link {:rel "stylesheet" :href "/assets/css/re-com.css"}]
                  [:link {:rel "stylesheet" :href "/assets/css/main.css"}]

                  [:link {:href "https://fonts.googleapis.com/css?family Roboto:300,400,500,700,400italic"
                          :rel "stylesheet" :type "text/css"}]
                  [:link {:href "https://fonts.googleapis.com/css?family Roboto+Condensed:400,300"
                          :rel "stylesheet" :type "text/css"}]

                  [:script {:src "/js/main.js" :type "text/javascript"}]]
     :background "#f6f6f6"
     :bootstrap-version 3}
    [:div#app {:style {:height "inherit"}}]
    [:script "window.prstr  = function (obj) { return cljs.core.pr_str(obj) };
              window.onload = function () { bud.client.core._main(); }"]))
