(set-env!
  :resource-paths #{"src"}
  :dependencies '[[org.clojure/clojure "1.7.0"]
                  [boot/core "2.5.5"]
                  [adzerk/bootlaces "0.1.13"]])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "1.0.0")

(task-options!
  pom {:project     'dgellow/boot-files
       :version     +version+
       :description "Boot task to do operations on files within the fileset (move, copy, etc)."
       :url         "https://github.com/dgellow/boot-files"
       :scm         {:url "https://github.com/dgellow/boot-files"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask build []
  (comp
   (pom)
   (jar)
   (target)
   (install)))
