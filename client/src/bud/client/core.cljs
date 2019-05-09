(ns ^:figwheel-hooks bud.client.core
  (:require [goog.dom :as gdom]
            [reagent.core :as r]
            [bud.client.views :as views]
            [bud.client.event :as event]))

(defn ^:after-load init! []
  (when-let [el (gdom/getElement "app")]
    (r/render [views/main] el)
    (.. js/firebase auth (onAuthStateChanged event/init!))))

(defn ^:export -main [& args]
  (init!))
