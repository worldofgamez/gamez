(ns gamez.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [cognitect.transit :as t]
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

(def page-info
  (let [loc (.-location js/window)
        ret {:host (.-host loc)
             :protocol (.-protocol loc)
             :ws (if (= "https:" (.-protocol loc)) "wss:" "ws:")
             }]
    ret))

(def cmd-dispatch (atom {}))

(def socket-guid (atom "yo"))

(def current-socket (atom nil))

(def send-queue (atom []))


(defn transit-encode
  "Encode the data"
  [data]
  (t/write (t/writer :json) data))

(defn transit-decode
  "Decode the data"
  [string]
  (t/read (t/reader :json) string))

(defn got-message
  [event]
  (let [msg (transit-decode (.-data event))
        cmd (:cmd msg)
        dispatch (@cmd-dispatch cmd)]
    (when dispatch
      (when (= "$" (.substring cmd 0 1))
        (swap! cmd-dispatch dissoc cmd))
      (dispatch (:data msg)))))

(def anti-forge (atom nil))

(defn send-msg
  "Send a message to the server"
  [msg]
  (if @current-socket
    (let [msg (assoc msg :anti-forge @anti-forge)]
      (.send @current-socket (transit-encode msg)))
    (swap! send-queue conj msg)))

(defn connect-socket
  "Connect to the socket"
  []

  (let [socket (js/WebSocket. (str (:ws page-info) "//"
                                   (:host page-info)
                                   "/api/1/socket/"
                                   @socket-guid))]
    (aset socket "onopen"
          (fn []
            (reset! anti-forge js/antiforgery)
            (reset! current-socket socket)
            (send-msg {:cmd "hello" :data (js/Date.)})
            (doseq [m @send-queue] (send-msg m))
            (reset! send-queue [])))

    (aset socket "onclose"
          (fn []
            (reset! current-socket nil)
            (connect-socket)
            ))

    (aset socket "onmessage"
          got-message)
    )
  )

(js/setTimeout connect-socket 50)

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
