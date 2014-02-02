(defproject lemmy "0.0.1-SNAPSHOT"
  :description "Cool new project to do things and stuff"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.match "0.2.1"]]
  :profiles {:dev {:dependencies [[midje "1.6.0"]]}
             :plugins  [[lein-midje "3.1.3"]]}
  :source-paths ["src" "src/lemmy"]
  :test-paths ["test" "test/lemmy"]
  :main lemmy.core)

