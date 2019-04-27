(ns bud.views
  (:require [bud.color :as color]))

(defn navbar [{:keys [show-login? dark? show-logout?]}]
  [:nav.navbar.static-top
   {:class (when (not dark?) "navbar-light bg-light")
    :style (when dark? {:background-color color/primary
                        :color "white"})}
   [:div.container
    [:a.navbar-brand {:href "/"
                      :style (cond-> {:font-size "24px"}
                               dark? (assoc :color "white"))}
     "FlexBudget"]
    (when show-login?
      [:a.btn.btn-primary {:href "/login"} "Sign In"])
    (when show-logout?
      [:a.btn.btn-primary {:href "/logout"} "Sign Out"])]])
