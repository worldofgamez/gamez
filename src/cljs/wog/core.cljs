(ns wog.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType])
    (:import goog.History))

;; -------------------------
;; Views

(defn home-page []
  [:div
   [:h2 "Your GameZ"]


   [:div.row
    [:div.col-md-2 {:style {:box-shadow "0 0 30px black"}} "Yak"]
    [:div.col-md-1]
    [:div.col-md-2 {:style {:box-shadow "0 0 30px black"}} "Yak"]
    [:div.col-md-1]
    [:div.col-md-2 {:style {:box-shadow "0 0 30px black"}} "Yak"]
    [:div.col-md-1]
    [:div.col-md-2 {:style {:box-shadow "0 0 30px black"}} "Yak"]
    ]
   ])

(defn story-page []
  [:div
   [:h2 "About World of GameZ"]
   "World of GameZ was founded when Daniel and Sophia's evil parents..."
   ])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/story" []
  (session/put! :current-page #'story-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
