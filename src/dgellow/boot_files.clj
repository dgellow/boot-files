(ns dgellow.boot-files
  (:require [clojure.java.io :as io]
            [boot.core :as core :refer [deftask]]
            [boot.util :as util]))

(defn log
  ([fn-name msg] (log fn-name msg nil))
  ([fn-name msg lvl]
   ((if (= :warn lvl) util/warn util/info)
    (format "[%s] %s" fn-name msg))))

(deftask move-files
  "Move files within the fileset."
  [f files PATH=DEST #{[str str]} "A map of files and their destination."]
  (let [files-map (into {} files)
        fn-log (partial log "move-files")]
    (fn middleware [next-handler]
      (fn handler [fileset]
        (if-not files
          (do
            (fn-log "Missing :files param, do nothing.\n" :warn)
            (next-handler fileset))
          (let [in-files (core/input-files fileset)]
            (fn-log "Move files\n")
            (->> (reduce
                (fn [fs [from to]]
                  (util/info (format "• %s -> %s\n" from to))
                  (try
                    (core/mv fs from to)
                    (catch java.lang.Exception e
                      (throw (java.io.FileNotFoundException.
                              (format "File %s not found in Boot fileset."
                                      from))))))

                fileset
                (seq files))
               core/commit!
               next-handler)))))))

(deftask copy-files
  "Copy files within the fileset."
  [f files PATH=DEST #{[str str]} "A map of files and their destination."]
  (let [files-map (into {} files)
        fn-log (partial log "copy-files")
        tmp (core/tmp-dir!)]
    (fn middleware [next-handler]
      (fn handler [fileset]
        (if-not files
          (do (fn-log "Missing :files param, do nothing.\n" :warn)
              (next-handler fileset))
          (do
            (core/empty-dir! tmp)
            (util/info "Copy files\n")
            (doseq [[from to] (seq files)]
              (util/info (format "• %s -> %s\n" from to))
              (let [in-files (core/input-files fileset)
                    in-file (first (core/by-path [from] in-files))]
                (when (not in-file)
                  (throw (java.io.FileNotFoundException.
                          (format "File %s not found in Boot fileset." from))))
                (let [in-tmp (core/tmp-file in-file)
                      out-file (io/file tmp to)]
                  (doto out-file
                    io/make-parents
                    (spit (slurp in-tmp))))))
            (-> fileset
               (core/add-resource tmp)
               core/commit!
               next-handler)))))))
