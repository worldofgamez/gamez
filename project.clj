(defproject gamez "0.1.0-SNAPSHOT"
  :description "World of GameZ core server"
  :url "http://worldofgamez.org"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src"]

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48" :scope "provided"]
                 [http-kit "2.1.18"]
                 [com.fasterxml.jackson.core/jackson-core "2.5.4"]
                 [com.fasterxml.jackson.core/jackson-annotations "2.5.0"]
                 [commons-logging "1.1.3"]

                 [org.apache.httpcomponents/httpclient "4.3.5"]
                 [org.slf4j/slf4j-jdk14 "1.7.12"]

                 [dragonmark/data "0.1.2"]
                 [dragonmark/inputs "0.4.0"]

                 [com.cemerick/url "0.1.1"]

                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 ;; dependency related
                 [prismatic/schema "0.4.3"]
                 [commons-codec "1.10"]
                 [prismatic/plumbing "0.4.4"]

                 [venantius/accountant "0.1.1"]

                 [ring-server "0.4.0"]
                 [cljsjs/react "0.13.3-1"]
                 [reagent "0.5.0"]
                 [reagent-forms "0.5.5"]
                 [reagent-utils "0.1.5"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-rewrite "0.1.0"]
                 [prone "0.8.2"]
                 [compojure "1.4.0"]
                 [hiccup "1.0.5"]
                 [environ "1.0.0"]

                 [secretary "1.2.3"]]

  :plugins [[lein-environ "1.0.0"]
            [lein-asset-minifier "0.2.2"]]

  :ring {:handler gamez.rest.handler/app
         :uberwar-name "gamez.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "gamez.jar"

  :main gamez.rest.server

  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :app :compiler :output-dir]
                                    [:cljsbuild :builds :app :compiler :output-to]]

  :minify-assets
  {:assets
    {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {:builds {:app {:source-paths ["src"]
                             :compiler {:output-to     "resources/public/js/app.js"
                                        :output-dir    "resources/public/js/out"
                                        :asset-path   "/js/out"
                                        :optimizations :none
                                        :pretty-print  true}}}}

  :profiles {:dev {:repl-options {:init-ns gamez.repl
                                  :nrepl-middleware []}

                   :dependencies [[ring/ring-mock "0.2.0"]
                                  [ring/ring-devel "1.4.0"]
                                  [lein-figwheel "0.3.7"]
                                  [org.clojure/tools.nrepl "0.2.10"]
                                  [pjstadig/humane-test-output "0.7.0"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.3.7"]
                             [lein-cljsbuild "1.0.6"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :nrepl-port 7002
                              :css-dirs ["resources/public/css"]
                              :ring-handler gamez.rest.handler/app}

                   :env {:dev true}

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler {:main "gamez.dev"
                                                         :source-map true}}
}
}}

             :uberjar {:hooks [leiningen.cljsbuild minify-assets.plugin/hooks]
                       :env {:production true}
                       :aot :all
                       :omit-source true
                       :cljsbuild {:jar true
                                   :builds {:app
                                             {:source-paths ["env/prod/cljs"]
                                              :compiler
                                              {:optimizations :advanced
                                               :pretty-print false}}}}}})
