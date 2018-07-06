
(set-env!
 :resource-paths #{"resource" "source"}
 :source-paths   #{"test"}
 :dependencies   '[[org.clojure/clojure     "1.8.0"  :scope "provided"]
                   [adzerk/boot-test        "1.2.0"  :scope "test"]
                   [adzerk/bootlaces        "0.1.13" :scope "test"]
                   [boot/core               "2.7.1"  :scope "provided"]
                   [familiar                "0.1.0"]
                   [stencil                 "0.5.0"]
                   ;; REPL dependencies.
                   [org.clojure/tools.nrepl "0.2.12" :scope "test"]]
 :repositories   #(conj % ["clojars" {:url "https://clojars.org/repo/"}]))

(require '[boot-aws-lambda-kit.boot-build :refer :all])
