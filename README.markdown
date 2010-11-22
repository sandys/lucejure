How to use this code
====================
1. Clone this repository
2. Install "leiningen" - (https://github.com/technomancy/leiningen). Leiningen is a self-contained script. The only dependency you need is Java.
3. run "lein deps" in this directory - to pull the lein-run plugin and  all dependencies will be downloaded automatically.
4. "lein run" will create your indexes (HACK: initially hardcoded to /tmp/2). if it doesnt work, run "lein run nihsearch -main"

How to create indexes manually (for verification)
=================================================
1. Lucene index creation 

    `cd /tmp/3/; java -cp /home/user/research/search/3/lib/lucene-core-3.0.0.jar:/home/user/research/search/lucene-3.0.2/lucene-demos-3.0.2.jar org.apache.lucene.demo.IndexFiles /home/user/Documents/articles/BMC_Clin_Pharmacol`

2. SemanticVector index creation

    `java -cp /home/user/research/search/3/lib/semanticvectors-1.30.jar:/home/user/research/search/3/lib/lucene-core-3.0.0.jar:/home/user/research/search/lucene-3.0.2/lucene-demos-3.0.2.jar pitt.search.semanticvectors.BuildIndex  /tmp/3/index`

3. SemanticVector search

    `java -cp /home/user/research/search/3/lib/semanticvectors-1.30.jar:/home/user/research/search/3/lib/lucene-core-3.0.0.jar:/home/user/research/search/lucene-3.0.2/lucene-demos-3.0.2.jar pitt.search.semanticvectors.Search agreements  -luceneindexpath /tmp/3/index`

