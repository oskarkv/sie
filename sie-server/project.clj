(defproject sieserver "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [http-kit "2.1.21-alpha2"]
                 [org.clojure/data.json "0.2.6"]]
  :main sieserver.core
  :profiles {:uberjar {:aot :all}})
