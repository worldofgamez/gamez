(ns gamez.rest.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.util.response :refer [file-response resource-response redirect]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [gamez.model.db :as db]
            [ring.middleware.reload :refer [wrap-reload]]
            [org.httpkit.server :as kit]
            [dragonmark.util.props :as dp]
            [clojure.data.json :as json]
            [environ.core :refer [env]])
  (:import [java.io File InputStream]
           (clojure.lang Var$Unbound)))

(set! *warn-on-reflection* true)

(def array-bytes (Class/forName "[B"))

(defn string-from-bytes
  [^"[B"  bytes]
  (String. bytes "UTF-8"))

(defn- materialize
  [x]
  (cond
    (string? x) x
    (instance? array-bytes x) (string-from-bytes x)
    (instance? File x) (slurp x)
    (instance? InputStream x) (slurp x)))

(defn- fix-binding
  "If it's an unbound var, then return a String"
  [v]
  (if (instance? Var$Unbound v) "Unbound" v))



(def home-page
  (html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (include-css (if (env :dev) "css/site.css" "css/site.min.css"))]
    [:body
     [:div#app
      "Howdy!"
      [:div#gamez_content]
      [:div "After the content DIV"]
      ]
     ]]))

(def commands (atom {}))

(defn do-socket [req guid]
  (kit/with-channel req channel
    (when (kit/websocket? channel)
      (let [cleanup (atom [])
            guid-info (db/rget guid)

            [guid guid-info]
            (if guid-info
              (do
                ;; FIXME bind to listeners
                [guid (atom guid-info)])
              (let [g (->
                       (java.util.UUID/randomUUID)
                       .toString)
                    s (atom {})]
                (db/rbind-atom g s)

                (kit/send! channel
                           (db/transit-encode
                            {:cmd "setGuid",
                             :data g}))
                [g s]))]
        (kit/on-close
         channel
         (doseq [the-fn @cleanup]
           (the-fn)))

        (kit/on-receive
         channel
         (fn [data]
           (let [to-do (db/transit-decode data)
                 the-cmds @commands]
             (println "Got " (pr-str to-do))
             (some-> to-do :cmd the-cmds
                     (apply [(:data to-do)
                             guid-info
                             (fn [x] (kit/send! channel (db/transit-encode x)))
                             guid])))))))))

(defn- fix-body
  [resp]
  (let [body (materialize (:body resp))]
    (assoc
     resp
     :body
     (clojure.string/replace body #"<body>"
                             (str "<body>\n<script type='text/javascript'>\nantiforgery = '"
                                  (fix-binding ring.middleware.anti-forgery/*anti-forgery-token*)
                                  "';\n</script>\n")))))


(defroutes routes
  (GET "/"
       []
       (fn [_]
         (let [resource-path "public/index.html"
               ret (resource-response resource-path)
               ret (fix-body ret)
               ret (assoc-in ret [:header "Content-Length"] (count (:body ret)))
               ret (assoc-in ret [:headers "Content-Type"] "text/html; utf-8")
               ret (assoc-in ret [:headers "Last-Modified"] "")]

           ret)))

  (GET "/api/1/socket/:guid" [guid]
       (fn [req] (do-socket req guid)))

  (resources "/")
  (not-found "Not Found"))

(def app
  (let [handler (wrap-defaults #'routes site-defaults)]
    (if (env :dev) (-> handler wrap-exceptions wrap-reload) handler)))
