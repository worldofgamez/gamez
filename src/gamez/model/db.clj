(ns gamez.model.db
  (:require
   [cognitect.transit :as t]
   [datomic.api :only [q db] :as d]
   [dragonmark.util.props :as dp]
   [taoensso.carmine :as car :refer (wcar)])
  (:import [java.io ByteArrayInputStream ByteArrayOutputStream]))

(set! *warn-on-reflection* true)

(defmacro wcar* [& body] `(car/wcar (:redis @dp/info)  ~@body))

(defn rget
  "Get a key from Redis"
  [key]
  (wcar* (car/get key)))

(defn rput
  "put a value in Redis"
  [key value]
  (wcar* (car/set key value)))



(defn transit-encode
  "Encodes a Clojure data structre as Transit data"
  [data]
  (let [out (ByteArrayOutputStream. 4096)
        writer (t/writer out :json)]
    (t/write writer data)
    (String. (.toByteArray out) "UTF-8")))

(defn transit-decode
  "Takes a String formatted as transit data and decodes it"
  [^String str]
  (let [in (ByteArrayInputStream. (.getBytes str "UTF-8"))
        reader (t/reader in :json)]
    (t/read reader)))


(defn rpub
  "Publish a value to a Redis queue"
  [key value]
  (wcar* (car/publish key (transit-encode value))))

(defn rsub
  "Subscribe to a patterned redis queue such that each
time a message arrives on the queue, it's transit encoded
and passed to the function (in a future). The function returns
  a function that will unsubscribe the function."
  [pat fnc]
  (let [listener (car/with-new-pubsub-listener (-> @dp/info :redis :spec)
                   {pat (fn [msg] (fnc (transit-decode msg)))}
                   (car/psubscribe pat))]
    (fn []
      (car/with-open-listener listener
        (car/unsubscribe))
      (car/close-listener listener))))

(defn rbind-atom
  "Write the contents of an Atom to a key in Redis"
  [key a]
  ;; write changes to Redis
  (rput key @a)
  (add-watch a :watcher
             (fn [key atom
                  old-state
                  new-state]
               (future
                 (rpub key new-state))
               (future
                 (wcar*
                  (car/set key new-state))))))

(defn d-url
  "The Datomic URL"
  []
  (-> @dp/info :datomic :url))

(def dconn
  "The Datomic connection"
  (delay
    (do
      (d/create-database (d-url))
      (d/connect (d-url)))))

      (defn dconn2
        "The Datomic connection"
        []

          (do
            ;; (d/create-database (d-url))
            (d/connect (d-url))))
