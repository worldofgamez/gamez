(ns gamez.util.shared
  (:require #?@(:clj
                [[dragonmark.util.props :as dp]
                 [environ.core :refer [env]]
                 [taoensso.carmine.ring :as car-ring]]))
   #?(:clj
      (:import (java.util Date))))

#?(:clj
   (defn extra-security
     [x]
     (if (env :dev)
       x
       (->
         x
         (assoc-in [:session :cookie-attrs :secure] true)
         (assoc-in [:session :cookie-name] "secure-ring-session")))))

#?(:clj
   (defn redis-sessions
     "Build a Ring session handler for Redis. Optional number of minutes for an inactivity timeout.
  Defaults to 30 minute timeout"
     ([] (redis-sessions 30))
     ([exp] (car-ring/carmine-store (:redis @dp/info) :expiration-secs (* exp 60)))))

(defn ten-year-old
  []
  (-> #?(:cljs (.now js/Date)) #?(:clj (System/currentTimeMillis))
      (- (* 1000 365 24 60 60 10))
      #?(:cljs (js/Date.)) #?(:clj (Date.))))