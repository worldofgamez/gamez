(ns gamez.client.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [dragonmark.inputs.core :as dim]
            [schema.core :as s]
            [cognitect.transit :as t]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [accountant.core :as accountant]
            [goog.history.EventType :as EventType])
  (:import goog.History))

;; -------------------------
;; Views

(declare href)

(defn home-page []
  [:div [:h2 "Welcome to World of GameZ"]
   [:div (href ::story-page)]
   [:div (href ::signup)]
   ])

(defn story-page []
  [:div [:h2 "About Gamez"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn ten-year-old
  []
  (-> (.now js/Date) (- (* 1000 365 24 60 60 10)) (js/Date.)))

(defn signup []
  (let [c (dim/make-input-comp
            :sign_up
            {:email  s/Str
             :name s/Str
             :birthday s/Inst
             :password s/Str}
            #(.log js/console (pr-str %))
            {:order       [:name :email :password :birthday]
             :email       {:type "email"}

             :password    {:type "password"}
             :validations [[:before (ten-year-old) :birthday]
                           [:min-length 8 :password]
                           [:email :email]]}
            )]
    [:div "Sign up"
     [:h2 [c]]
     ]))



(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
;; (secretary/set-config! :prefix "#")

(defonce main-state (atom {}))

(def pages (atom {}))

(defn route-from
  [item]
  (->> item keys (filter string?) first))

(defn href
  [where]
  (let [w (@pages where)]
    (if (and  w ((or (:when w) #(-> true)))) [:a {:href (route-from w)} (or (:name w) (route-from w))]
                                             "")))

(def routes
  [{"/" #'home-page :name "Home"}
   {"/t/story" #'story-page :name "Our Story"}
   {"/t/signup" #'signup :name "Sign Up"
    :when #(-> @main-state :logged-in not)}
   {"/t/*" #'home-page :name "Go Home" :when #(-> false)}])

(defn build-secretary-routes
  [{:keys [kids] :as item}]
  (let [route (route-from item)
        page (item route)
        kw (-> page .-sym keyword)
        legal (or (:when item) #(-> true))]
    (swap! pages assoc kw item)
    (doseq [k kids] (build-secretary-routes k))
    (secretary/add-route!
      route
      (fn [& _]
        (if (legal)
          (session/put! :current-page page)
          (do
            (-> js/window .-history (.pushState nil "Home" "/"))
            (secretary/dispatch! "/")))))))

(mapv build-secretary-routes routes)

;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(accountant/configure-navigation!)

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))

(js/setTimeout
  #(let [path (-> js/window .-location .-pathname)]
    (secretary.core/dispatch! path))
  50
  )
(def page-info
  (let [loc (.-location js/window)
        ret {:host (.-host loc)
             :protocol (.-protocol loc)
             :ws (if (= "https:" (.-protocol loc)) "wss:" "ws:")
             }]
    ret))

(def socket-guid (atom "yo"))

(def set-guid #(reset! socket-guid %))

(def cmd-dispatch (atom {"setGuid" set-guid}))

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
    (.log js/console (pr-str msg))
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
    (.log js/console "Yo!")
    (aset socket "onopen"
          (fn []
            (.log js/console "open")
            (reset! anti-forge js/antiforgery)
            (reset! current-socket socket)
            (send-msg {:cmd "hello" :data (js/Date.)})
            (doseq [m @send-queue] (send-msg m))
            (reset! send-queue [])))

    (aset socket "onclose"
          (fn []
            (.log js/console "Closed")
            (reset! current-socket nil)
            (connect-socket)))

    (aset socket "onmessage"
          got-message)
    )
  )

(js/setTimeout connect-socket 50)


