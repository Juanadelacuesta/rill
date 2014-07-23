(defproject studyflow/learning "0.1.0-SNAPSHOT"
  :description "Event-sourced learning application"
  :url "http://studyflow.nl/"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-http "0.9.1"]
                 [cheshire "5.3.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [environ "0.5.0"]
                 [prismatic/schema "0.2.2"]
                 [ring/ring-json "0.3.1"]
                 [compojure "1.1.8"]
                 [ring/ring-jetty-adapter "1.3.0"]
                 [clout-link "0.0.6"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.slf4j/slf4j-log4j12 "1.7.5"]
                 [ring-mock "0.1.5"]
                 [ring/ring-devel "1.2.1"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [com.stuartsierra/component "0.2.1"]
                 [identifiers "1.0.0"]
                 [rill "0.1.0-SNAPSHOT"]]

  :profiles {:dev
             {:source-paths ["dev"]
              :resource-paths ["dev/resources"]
              :dependencies
              [[org.clojure/tools.trace "0.7.5"]
               [org.clojure/tools.namespace "0.2.5"]
               [org.clojure/clojurescript "0.0-2173"]
               [om "0.6.4"]
               [com.facebook/react "0.9.0.1"]
               [cljs-ajax "0.2.3"]
               [cljs-uuid "0.0.4"]]
              :plugins
              [[lein-cljsbuild "1.0.2"]
               [com.cemerick/clojurescript.test "0.3.0"]]}
             :uberjar {:aot [studyflow.main]
                       :main studyflow.main}}
  :test-paths ["test"]
  :aliases {"server" ["run" "-m" "studyflow.main"]
            "validate-course-material" ["run" "-m" "studyflow.cli.validate-course-material-json"]}
  :cljsbuild {:builds {:dev {:source-paths ["cljs/src"]
                             :compiler {:output-to "resources/public/js/studyflow-dev.js"
                                        :output-dir "resources/public/js/out"
                                        :optimizations :whitespace}}
                       :prod {:source-paths ["cljs/src"]
                              :compiler {:output-to "resources/public/js/studyflow.js"
                                         :optimizations :advanced
                                         :elide-asserts true
                                         :pretty-print false
                                         :preamble ["react/react.min.js"]
                                         :externs ["react/externs/react.js"]}}}})