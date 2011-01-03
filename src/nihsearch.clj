(ns nihsearch
  (:import (org.apache.lucene.index IndexReader$FieldOption IndexReader IndexWriter
                                    IndexWriter$MaxFieldLength Term CorruptIndexException)
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
           (java.util Collection Random)
           (java.io File File PushbackReader IOException FileNotFoundException ))
  (:import [pitt.search.semanticvectors BuildIndex])
  (:use 
        clojure.contrib.str-utils
        
        clojure.contrib.def)
  (:require [ clojure.contrib.duck-streams :as ds] 
            [ clojure.contrib.seq-utils :as seq])
  (:gen-class))


(def *config* nil)

(defn cljLoadStopWords [stopset]
  (do
    (println "Using stopword file: " (:stoppath *config*))
    ( reduce  (fn [val coll] (conj val coll) )  stopset (ds/read-lines (:stoppath *config*) ) )))

(defn lazy-seq-terms [terms] (lazy-seq (when (.next terms) (cons (.term terms) (lazy-seq-terms terms)) )))

(defn cljTermFilter [term nonAlphabet fieldsToIndex] 
  (and 
   ;need to put stoplistContains here
   ;need to put getGlobalTermFreq here
    (not (> (reduce 0 
                    (fn [accum curr] 
                       (if (Character/isLetter curr)
                          1 0) ) 
                    (.text term)) nonAlphabet))
    (first (filter (fn [d] (= 0 (.compareToIgnoreCase (.field term) d)) ) fieldsToIndex))))

(defn _populateIndexVectors [ir nonAlphabet fieldsToIndex]
  (seq (set ( filter #(println (.text %))  (lazy-seq-terms (.terms ir)) ))))
  ;(seq (set ( filter #(cljTermFilter % nonAlphabet fieldsToIndex)  (lazy-seq-terms (.terms ir)) ))))

(defn cljTermTermVectorsFromLucene [indexDir seedLength minFreq nonAlphabet windowSize basicTermVectors fieldsToIndex]
;correct place for validation of arguments is in pre-conditions
 (do  
    (with-open [iw   (IndexWriter. (FSDirectory/open (File. (str indexDir))) (StandardAnalyzer. Version/LUCENE_30) false IndexWriter$MaxFieldLength/LIMITED)]
          (try (.optimize iw)
            (catch CorruptIndexException e
              (.printStackTrace e))))
    (with-open [ir (IndexReader/open (FSDirectory/open (File. indexDir)))] 
      (let [^Collection fields_with_positions (.getFieldNames ir IndexReader$FieldOption/TERMVECTOR_WITH_POSITION)  
          random (Random.) termVectors {} ]
        (when (.isEmpty fields_with_positions)
          (throw (IOException. (str "Lucene indexes not built correctly." 
                                    "Term-term indexing requires a Lucene index containing TermPositionVectors." 
                                    "Try rebuilding Lucene index using pitt.search.lucene.IndexFilePositions."))))
        (_populateIndexVectors ir nonAlphabet fieldsToIndex)
        ))))

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
    (with-open [iw   (IndexWriter. (FSDirectory/open (File. (:indexdir *config*))) (StandardAnalyzer. Version/LUCENE_30) IndexWriter$MaxFieldLength/LIMITED
                          ;(.setRAMBufferSizeMB 20)
                          ;(.setUseCompoundFile false)
                          )  ](.addDocument iw  (doto doc (.add (Field. "path" (.getPath file)  Field$Store/YES Field$Index/NOT_ANALYZED))
                                                          (.add (Field. "contents" (:msg pf) Field$Store/YES Field$Index/ANALYZED ) ))) )))

(defn sem-index [^File file]
  (BuildIndex/main (into-array String ["/tmp/2" ])))

(defn -main [& args] 
  (alter-var-root #'*config* (fn [_] (read (PushbackReader. (ds/reader "config.clj")))))
  (let [{srcdir :srcdir} *config* {indexdir :indexdir} *config* {stoppath :stoppath} *config* 
       stopset (cljLoadStopWords #{}) ]
      
    (IndexWriter/unlock (FSDirectory/open  (File. (:indexdir *config*)))) ;if not, then pre-existing "write.lock" files can cause lock-acquisition timeouts
    (println "srcdir= " srcdir " indexdir= " indexdir "stoppath= " stoppath)
    (map println stopset)
    ;(map index (walk (File. (:srcdir *config*))) )
    ;(sem-index (File. "/tmp/3") )
    ;(with-open [ir (IndexReader/open (FSDirectory/open (File. "/tmp/3")))] 
    ;  (_populateIndexVectors ir 0 (into-array ["n"])) )
    ) )
