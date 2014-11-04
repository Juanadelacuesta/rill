(defproject rekenmachien "0.1.0-SNAPSHOT"
  :source-paths ["src" "dev"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.cemerick/clojurescript.test "0.3.1"]
                 [ring "1.3.1"]
                 [compojure "1.2.0"]
                 [figwheel "0.1.4-SNAPSHOT"]
                 [environ "1.0.0"]
                 [leiningen "2.5.0"]
                 [reagent "0.4.2"]]

  :min-lein-version "2.5.0"

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-environ "1.0.0"]
            [lein-figwheel "0.1.4-SNAPSHOT"]]

  :figwheel {:http-server-root "public"
             :port 3449}

  :cljsbuild {:builds [{:id "app"
                        :source-paths ["src" "dev"]
                        :compiler {:output-to "resources/public/js/app.js"
                                   :output-dir "resources/public/js/out"
                                   :source-map "resources/public/js/out.js.map"}}
                       {:id "tests"
                        :source-paths ["src" "test"]
                        :compiler {:optimization :simple
                                   :preamble ["reagent/react.js"]
                                   :output-to "resources/public/js/tests.js"
                                   :output-dir "resources/public/js/tests"
                                   :source-map "resources/public/js/tests.js.map"}}]})
