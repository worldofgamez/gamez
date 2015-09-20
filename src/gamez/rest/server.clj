(ns gamez.rest.server
  (:use [org.httpkit.server :refer :all]
        [ring.middleware file-info file])
  (:require
    [gamez.rest.handler :as gamez]
    [environ.core :refer [env]]
    [clojure.tools.logging :as log])
  (:gen-class)
  )

(defonce server (atom nil))


(defn get-handler []
  ;; #'app expands to (var app) so that when we reload our code,
  ;; the server is forced to re-resolve the symbol in the var
  ;; rather than having its own copy. When the root binding
  ;; changes, the server picks it up without having to restart.
  (-> #'gamez/app
      ; Content-Type, Content-Length, and Last Modified headers for files in body
      (wrap-file-info)))

(defn start-server
  "used for starting the server in development mode from REPL"
  [& [port]]
  (let [port (if port (Integer/parseInt port) 3000)]
    (reset! server
            (run-server (get-handler)
                        {:port port
                         :auto-reload? true
                         :join? false}))
    (log/info (str "Started forum server on port: " port)))
  )

(defn stop-server []
  (when @server
    (.stop @server))
  (reset! server nil)
  )

(defn -main [& args]
  (System/setProperty "java.util.logging.SimpleFormatter.format" "[%1$tF %1$tr] %3$s %4$s:  %5$s %n")
  (apply start-server args))


