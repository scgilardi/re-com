(ns re-demo.datepicker
  (:require
    [goog.date.Date]
    [reagent.core      :as    reagent]
    [reagent.ratom     :refer-macros [reaction]]
    [cljs-time.core    :refer [now days minus day-of-week]]
    [cljs-time.format  :refer [formatter unparse]]
    [re-com.core       :refer [h-box v-box box gap single-dropdown datepicker datepicker-dropdown checkbox label title p]]
    [re-com.datepicker :refer [iso8601->date datepicker-args-desc]]
    [re-demo.utils     :refer [panel-title title2 args-table github-hyperlink status-text]]))


(def ^:private days-map
     {:Su "S" :Mo "M" :Tu "T" :We "W" :Th "T" :Fr "F" :Sa "S"})


(defn- toggle-inclusion!
  "convenience function to include/exclude member from"
  [set-atom member]
  (reset! set-atom
          (if (contains? @set-atom member)
            (disj @set-atom member)
            (conj @set-atom member))))


(defn- checkbox-for-day
  [day enabled-days]
  [v-box
   :align    :center
   :children [[label
               :style {:font-size "smaller"}
               :label (day days-map)]
              [checkbox
               :model     (@enabled-days day)
               :on-change #(toggle-inclusion! enabled-days day)]]])

(defn- parameters-with
  "Toggle controls for some parameters."
  [content enabled-days disabled? show-today? show-weeks?]
  [v-box
   :gap      "15px"
   :align    :start
   :children [[gap :size "20px"]
              [title :level :level3 :label "Parameters"]
              [h-box
               :gap      "20px"
               :align    :start
               :children [[checkbox
                           :label     [box :align :start :child [:code ":disabled?"]]
                           :model     disabled?
                           :on-change #(reset! disabled? %)]
                          [checkbox
                           :label     [box :align :start :child [:code ":show-today?"]]
                           :model     show-today?
                           :on-change #(reset! show-today? %)]
                          [checkbox
                           :label     [box :align :start :child [:code ":show-weeks?"]]
                           :model     show-weeks?
                           :on-change #(reset! show-weeks? %)]]]
              [h-box
               :gap      "2px"
               :align    :center
               :children [[checkbox-for-day :Su enabled-days]
                          [checkbox-for-day :Mo enabled-days]
                          [checkbox-for-day :Tu enabled-days]
                          [checkbox-for-day :We enabled-days]
                          [checkbox-for-day :Th enabled-days]
                          [checkbox-for-day :Fr enabled-days]
                          [checkbox-for-day :Sa enabled-days]
                          [gap :size "5px"]
                          [box :align :start :child [:code ":selectable-fn"]]]]
              [:span [:code "e.g. (fn [date] (#{1 2 3 4 5 6 7} (day-of-week date)))"]]
              content]])


(defn- date->string
  [date]
  (if (instance? js/goog.date.Date date)
    (unparse (formatter "dd MMM, yyyy") date)
    ""))



(defn- show-variant
  [variation]
  (let [model1          (reagent/atom (minus (now) (days 3)))
        model2          (reagent/atom (iso8601->date "20140914"))
        disabled?       (reagent/atom false)
        show-today?     (reagent/atom true)
        show-weeks?     (reagent/atom false)
        enabled-days    (reagent/atom (-> days-map keys set))
        as-days         (reaction (-> (map #(% {:Su 7 :Sa 6 :Fr 5 :Th 4 :We 3 :Tu 2 :Mo 1}) @enabled-days) set))
        selectable-pred (fn [date] (@as-days (day-of-week date))) ; Simply allow selection based on day of week.
        label-style     {:font-style "italic" :font-size "smaller" :color "#777"}]
    (case variation
      :inline [(fn inline-fn
                 []
                 [parameters-with
                  [h-box
                   :gap      "20px"
                   :align    :start
                   :children [[(fn []
                                 [v-box
                                  :gap      "5px"
                                  :children [[label :style label-style :label ":minimum or :maximum not specified"]
                                             [datepicker
                                              :model         model1
                                              :disabled?     disabled?
                                              :show-today?   @show-today?
                                              :show-weeks?   @show-weeks?
                                              :selectable-fn selectable-pred
                                              :on-change     #(reset! model1 %)]
                                             [label :style label-style :label (str "selected: " (date->string @model1))]]])]
                              ;; restricted to both minimum & maximum date
                              [(fn []
                                 [v-box
                                  :gap      "5px"
                                  :children [[label :style label-style :label ":minimum \"20140831\" :maximum \"20141019\""]
                                             [datepicker
                                              :model         model2
                                              :minimum       (iso8601->date "20140831")
                                              :maximum       (iso8601->date "20141019")
                                              :show-today?   @show-today?
                                              :show-weeks?   @show-weeks?
                                              :selectable-fn selectable-pred
                                              :disabled?     disabled?
                                              :on-change     #(reset! model2 %)]
                                             [label :style label-style :label (str "selected: " (date->string @model2))]]])]]]
                  enabled-days
                  disabled?
                  show-today?
                  show-weeks?])]
      :dropdown [(fn dropdown-fn
                   []
                   [parameters-with
                    [h-box
                     :size     "auto"
                     :align    :start
                     :children [[gap :size "120px"]
                                [(fn []
                                   [datepicker-dropdown
                                    :model         model1
                                    :show-today?   @show-today?
                                    :show-weeks?   @show-weeks?
                                    :selectable-fn selectable-pred
                                    :format        "dd MMM, yyyy"
                                    :disabled?     disabled?
                                    :on-change     #(reset! model1 %)])]]]
                    enabled-days
                    disabled?
                    show-today?
                    show-weeks?])])))


(def variations ^:private
  [{:id :inline   :label "Inline"}
   {:id :dropdown :label "Dropdown"}])


(defn datepicker-examples
  []
  (let [selected-variation (reagent/atom :inline)]
    (fn examples-fn []
      [v-box
       :size     "auto"
       :gap      "10px"
       :children [[panel-title "Date Components"
                               "src/re_com/datepicker.cljs"
                               "src/re_demo/datepicker.cljs"]
                  [h-box
                   :gap      "100px"
                   :children [[v-box
                               :gap      "10px"
                               :width    "450px"
                               :children [[title2 "[datepicker ... ] & [datepicker-dropdown ... ]" {:font-size "24px"}]
                                          [status-text "Stable"]
                                          [p "An inline or popover date picker component."]
                                          [args-table datepicker-args-desc]]]
                              [v-box
                               :gap       "10px"
                               :size      "auto"
                               :children  [[title2 "Demo"]
                                           [h-box
                                            :gap      "10px"
                                            :align    :center
                                            :children [[label :label "Select a demo"]
                                                       [single-dropdown
                                                        :choices   variations
                                                        :model     selected-variation
                                                        :width     "200px"
                                                        :on-change #(reset! selected-variation %)]]]
                                           [show-variant @selected-variation]]]]]]])))


;; core holds a reference to panel, so need one level of indirection to get figwheel updates
(defn panel
  []
  [datepicker-examples])
