(ns nihsearch
  (:import (org.apache.lucene.index IndexReader IndexWriter
                                    IndexWriter$MaxFieldLength Term)
           (org.apache.lucene.search IndexSearcher BooleanQuery
                                     PhraseQuery BooleanClause$Occur TermQuery)
           (org.apache.lucene.document Document Field Field$Store
                                       Field$Index DateTools
                                       DateTools$Resolution)
           (org.apache.lucene.analysis SimpleAnalyzer)
           (org.apache.lucene.analysis.standard StandardAnalyzer)
           (org.apache.lucene.search.highlight QueryTermExtractor)
           (org.apache.lucene.store FSDirectory)
           (org.apache.lucene.queryParser QueryParser$Operator
                                          MultiFieldQueryParser)
           (org.apache.lucene.util Version)
           (java.io File File PushbackReader)
           (java.io.FileNotFoundException))
  (:import [pitt.search.semanticvectors BuildIndex])
  (:use clojure.contrib.duck-streams
        clojure.contrib.str-utils
        clojure.contrib.seq-utils
        clojure.contrib.def)
  (:gen-class))


(def *config* nil)

(defn walk [^File dir]
  (let [child (.listFiles dir)
        subdirs (filter (fn [childdir] (.isDirectory childdir)) child)
        subfiles (filter (fn [childdir] (.isFile childdir)) child) ]
          (concat subfiles (mapcat walk subdirs))))

(defn parse-file [^File file] 
  {:msg (slurp (.getAbsolutePath file))
   :id (.getAbsolutePath file) })

;dunno why I should use fieldpools ?

(defn index [^File file]
  (let [#^Document doc (Document.)
        pf (parse-file file)]
    ;(with-open [iw ( doto (IndexWriter. (FSDirectory/getDirectory "/tmp/2") (StandardAnalyzer.) IndexWriter$MaxFieldLength/UNLIMITED)
    (with-open [iw   (IndexWriter. (FSDirectory/open (File. "/tmp/2")) (StandardAnalyzer. Version/LUCENE_30) IndexWriter$MaxFieldLength/LIMITED
                          ;(.setRAMBufferSizeMB 20)
                          ;(.setUseCompoundFile false)
                          )  ](.addDocument iw  (doto doc (.add (Field. "path" (.getPath file)  Field$Store/YES Field$Index/NOT_ANALYZED))
                                                          (.add (Field. "contents" (:msg pf) Field$Store/YES Field$Index/ANALYZED ) ))) )))

(defn sem-index [^File file]
  (BuildIndex/main (into-array String ["/tmp/2" ])))

(defn -main [& args] 
  (alter-var-root #'*config* (fn [_] (read (PushbackReader. (reader "config.clj")))))
  ;(IndexWriter/unlock (FSDirectory/getDirectory  "/tmp/2")) ;if not, then pre-existing "write.lock" files can cause lock-acquisition timeouts
  (IndexWriter/unlock (FSDirectory/open  (File. "/tmp/2"))) ;if not, then pre-existing "write.lock" files can cause lock-acquisition timeouts
  (let [{srcdir :srcdir} *config*]
      
           (do (println "sss " srcdir)
               ;(map index (walk (File. "/home/user/Documents/articles/BMC_Clin_Pharmacol")) )
               (sem-index (File. "/tmp/2") )
               ) ) )
