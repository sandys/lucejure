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
           (java.io File File PushbackReader)
           (java.io.FileNotFoundException))
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
    (.add doc (Field. "id" (:id pf) Field$Store/YES Field$Index/TOKENIZED ))
    (.add doc (Field. "msg" (:msg pf) Field$Store/YES Field$Index/TOKENIZED  )) 
    (with-open [iw ( doto (IndexWriter. (FSDirectory/getDirectory "/tmp/2") (StandardAnalyzer.) IndexWriter$MaxFieldLength/UNLIMITED)
                          (.setRAMBufferSizeMB 20)
                          (.setUseCompoundFile false))  ](.updateDocument iw (Term. "id" (:id pf)) doc) )))

(defn -main [& args] 
  (alter-var-root #'*config* (fn [_] (read (PushbackReader. (reader "config.clj")))))
  (IndexWriter/unlock (FSDirectory/getDirectory  "/tmp/2")) ;if not, then pre-existing "write.lock" files can cause lock-acquisition timeouts
  (let [{srcdir :srcdir} *config*]
      
           (do (println "sss " srcdir)
               (map index (walk (File. "/home/user/Documents/articles/BMC_Clin_Pharmacol")) )) ) )
