(defproject rill "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [org.clojure/tools.logging "0.2.6"]
                 [prismatic/schema "0.2.2"]
                 [slingshot "0.10.3"]
                 [environ "0.5.0"]
                 [cheshire "5.3.1"]
                 [clj-http "0.9.1"]
                 [com.velisco/tagged "0.3.4"]
                 [me.raynes/conch "0.7.0"]
                 [identifiers "1.1.0"]
                 [org.clojure/java.jdbc "0.3.4"]
                 [postgresql "9.1-901.jdbc4"]
                 [com.taoensso/nippy "2.6.3"]]
  :aot [rill.cli])