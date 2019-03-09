
(ns boot-aws-lambda-kit.cljs-test
  (:require [clojure.java.io          :as io]
            [clojure.test             :as test :refer [deftest is testing]]
            [boot.pod                 :as pod]
            [boot.tmpdir              :as tmpdir]
            [boot-aws-lambda-kit.cljs :as balk :refer [handler]]
            [familiar.test])
  (:import  [java.nio.file Files]
            [java.nio.file.attribute FileAttribute]
            [boot.tmpdir TmpFileSet]))

(defn tempdir
  []
  (.toFile (Files/createTempDirectory "tmpdir" (into-array FileAttribute []))))

(defn make-fs
  []
  (let [dir (tempdir)]
    {:dir dir
     :fs  (TmpFileSet. [(tmpdir/map->TmpDir {:dir dir :input true})] {} (tempdir) {})}))

(defn spit-to [dir path contents]
  (spit (doto (apply io/file dir path) io/make-parents) contents))

(defn apply-handler
  [h & args]
  (apply (h identity) args))

(deftest test:no-cljs-build-config
  (let [mw (handler {:ids #{"foo"}})]
    (is (ex-thrown-with-data?
         #(= {:expected "foo.cljs.edn" :received '()} %)
         (apply-handler mw (:fs (make-fs)))))))

(deftest test:many-cljs-build-config
  (let [{:keys [dir fs]} (make-fs)
        d1 (doto (tempdir)
             (spit-to ["a" "foo.cljs.edn"] "content of a")
             (spit-to ["b" "foo.cljs.edn"] "content of b"))
        fs (-> fs (tmpdir/add dir d1 {}) (tmpdir/commit!))
        mw (handler {:ids #{"foo"}})]
    (is (ex-thrown-with-data?
         #(and (= "foo.cljs.edn" (get % :expected))
               (= 2 (count (get % :received))))
         (apply-handler mw fs)))))
