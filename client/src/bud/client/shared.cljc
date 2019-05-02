(ns bud.client.shared
  (:require [bud.client.color :as color]))

(defn navbar [& contents]
  [:nav.navbar.static-top
   {:style {:background-color color/primary
            :color "white"}}
   (into [:div.container
          [:a.navbar-brand {:href "/"
                            :style {:font-size "24px"
                                    :color "white"}}
           "FlexBudget"]]
         contents)])
