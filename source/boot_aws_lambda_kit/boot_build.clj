
(ns boot-aws-lambda-kit.boot-build
  (:refer-clojure :exclude [test])
  (:require [boot.core          :as core]
            [boot.task.built-in :as task]
            [adzerk.boot-test   :as test]
            [adzerk.bootlaces   :as laces]))

(def project 'boot-aws-lambda-kit)
(def version "0.1.0-SNAPSHOT")

(laces/bootlaces! version :dont-modify-paths? true)

(core/task-options!
 task/pom (let [url "https://github.com/sinistral/boot-aws-lambda-kit"]
             {:project     project
              :version     version
              :description "Boot task to package Clojure(Script) functions as AWS Lambda handlers."
              :url         url
              :scm         {:url url}
              :license     {"2-Clause BSD License" "https://opensource.org/licenses/BSD-2-Clause"}}))

(core/deftask test
  []
  (test/test))

(core/deftask build
  []
  (comp (test)
        (laces/build-jar)))

;;; ----------------------------------------------------------------------- ; ;;

(core/deftask deploy-snapshot
  []
  (comp (build)
        (task/push :repo "clojars"
                   :gpg-sign true
                   :gpg-user-id (System/getenv "CLOJARS_GPG_KEY_ID")
                   :ensure-snapshot true)))
