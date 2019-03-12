
(set-env!
 :resource-paths #{"resource" "source"}
 :source-paths   #{"test"}
 :dependencies   '[[org.clojure/clojure     "1.8.0"          :scope "provided"]

                   [boot/core               "2.7.1"          :scope "provided"]
                   [camel-snake-kebab       "0.4.0"]
                   [familiar                "0.1.0"]
                   [stencil                 "0.5.0"]

                   [sinistral/boot-test     "1.2.1-SNAPSHOT" :scope "test"]
                              ; https://github.com/adzerk-oss/boot-test/pull/36
                   [adzerk/bootlaces        "0.1.13"         :scope "test"]

                   ;; REPL dependencies.
                   [org.clojure/tools.nrepl "0.2.12"         :scope "test"]]
 :repositories   #(conj % ["clojars" {:url "https://clojars.org/repo/"}]))

(require '[boot-aws-lambda-kit.boot-build :refer :all])
