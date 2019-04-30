(ns ^:figwheel-hooks bud.client.core
  (:require [goog.dom :as gdom]
            [reagent.core :as r]
            [bud.client.views :as views]))

(defn ^:after-load init! []
  (when-let [el (gdom/getElement "app")]
    (r/render [views/main] el)))

(defn ^:export -main [& args]
  (init!))
