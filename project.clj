(defproject nihsearch "0.1.0-snapshot"
  :dependencies [[org.clojure/clojure "1.2.0"]
                  [org.clojure/clojure-contrib "1.2.0"]
                  [org.apache.lucene/lucene-core "3.0.0"]
                  ;[org.apache.lucene/lucene-core "2.9.2"]
                  ;[org.apache.lucene/lucene-highlighter "2.9.2"]]
                  [org.apache.lucene/lucene-highlighter "3.0.0"]]
  :dev-dependencies [[lein-run "1.0.0"]]
  :disable-implicit-clean true
  :namespaces :all
  :main nihsearch)
