(ns bud.client.views
  (:require [reagent.core :as r]
            [reagent.ratom :refer-macros [reaction]]
            [trident.views.re-com :as rc]
            [trident.util :as u]
            [bud.client.color :as color]
            [bud.client.db :as db]
            [bud.client.event :as event]
            [bud.client.shared :refer [navbar]]
            [clojure.string :refer [join lower-case]]
            [cljs-time.coerce :as coerce]
            [cljs-time.core :refer [plus today days]]))

(defn format-currency [amount]
  (u/format "%.2f" (/ amount 100)))

(defn header-title [label level]
  [rc/title
   :style {:color "white"
           :margin 0}
   :label label
   :level level])

(defn header []
  [rc/h-box {:style {:padding "15px 20px"
                     :background-color color/primary}
             :align :center}
   [header-title "FlexBudget" :level1]
   [rc/grow]
   [rc/hyperlink
    :style {:font-size "1.2em" :color "white"}
    :label "Log out"
    :on-click event/sign-out!]])

(def cell-style {:style {:padding-right 15}})

(defn table [header-row rows]
  [:table.table {:style {:width "initial"
                         :min-width "50%"}}
   [:thead
    [:tr (rc/for [th header-row] [:th cell-style th])]]
   [:tbody
    (rc/for [r rows]
      [:tr (rc/for [td r] [:td cell-style td])])]])

(defn parse-currency [s]
  (.round js/Math (* (js/parseFloat s) 100)))

(defn amount-input
  ([ent k]
   (let [amount (k ent)]
     [rc/input-text
      :model (when (not (contains? #{0 nil} amount)) (format-currency amount))
      :on-change #(event/save! (assoc ent k (parse-currency %)))
      :width "100px"
      :placeholder "0.00"]))
  ([ent]
   (amount-input ent :misc/amount)))

(defn description-input [ent]
  [rc/input-text
   :model (:misc/description ent)
   :on-change #(event/save! (assoc ent :misc/description %))
   :width "200px"
   :placeholder "description"])

(defn rm-ent [ent]
  [rc/button
   :label "Remove"
   :on-click #(event/rm! (:db/id ent))])

(defn add-ent [cb]
  [rc/button
   :label "Add"
   :on-click cb])

(defn assets []
  [rc/v-box {:style {:width "395px"}}
   (for [ent @db/assets]
     [rc/h-box
      [amount-input ent]
      [description-input ent]
      [rm-ent ent]])
   [add-ent event/asset!]])

(defn datepicker [{:keys [k ent] :as opts}]
  (let [opts (merge {:model (k ent)
                     :on-change #(event/save! (assoc ent k %))}
                    (select-keys opts [:placeholder :minimum]))]
    (reduce into [rc/datepicker-dropdown] opts)))

(defn deltas []
  [rc/v-box {:style {:width "900px"}}
   (for [ent @db/deltas]
     [rc/h-box
      [amount-input ent]
      [description-input ent]
      [rc/single-dropdown
       :choices [{:id :weekly :label "Weekly"}
                 {:id :monthly :label "Monthly"}
                 {:id :yearly :label "Yearly"}]
       :model (:delta/frequency ent)
       :on-change #(when % (event/save! (assoc ent :delta/frequency %)))
       :placeholder "Frequency"
       :width "150px"]
      [datepicker {:ent ent
                   :k :delta/basis
                   :placeholder "Starting date"}]
      [rm-ent ent]])
   [add-ent event/delta!]])

(defn entry []
  [rc/v-box
   [rc/title
    :label (str "Current entry: " (if @db/draft? "draft" "some date"))
    :level :level2]
   [assets]
   [deltas]])

(defn goals []
  [table
   ["Target date" "Goal amount" "Desired weekly allowance"]
   [[[datepicker {:ent @db/goal
                  :k :goal/date
                  :placeholder "Target date"
                  :minimum (plus (today) (days 1))}]
     [amount-input @db/goal]
     [amount-input @db/goal :goal/allowance]]]])

(defn result []
  [rc/label :label
   (u/format "Weekly allowance: %s (%s %s budget)"
             (format-currency @db/weekly-allowance)
             (format-currency (Math/abs @db/surplus))
             (if (neg? @db/surplus) "over" "under"))])

(defn main []
  (let [tab (r/atom ::assets)]
    (fn []
      [rc/v-box
       [header]
       (if @db/loading?
         [rc/throbber
          :style {:align-self "center"}
          :size :large :color color/primary]
         [rc/v-box {:width "900px" :margin "0 auto"}
          [rc/gap :size "10px"]
          (when @db/goal-complete? [result])
          [rc/gap :size "0px"]
          [rc/horizontal-tabs
           :model tab
           :tabs [{:id ::assets :label "Current funds"}
                  {:id ::recurring :label "Income & Expenses"}
                  {:id ::goals :label "Goals"}]]
          (rc/case @tab
            ::assets [assets]
            ::recurring [deltas]
            ::goals [goals])
          [rc/gap]])])))
