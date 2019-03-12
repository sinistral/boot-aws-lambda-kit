
(ns boot-aws-lambda-kit.cljs
  "In which is defined the Boot task to package CLJS functions as AWS Lambda
  handlers."
  (:require [clojure.string  :as string]
            [clojure.java.io :as io]
            [boot.core       :as boot-core]
            [boot.util       :as boot-util :refer [info]]
            [camel-snake-kebab.core :as csk :refer [->snake_case]]
            [familiar.core   :as familiar :refer [fmtstr single!]]
            [stencil.core    :as stencil]))

(def handler-key :boot-aws-lambda-kit/handler)

(defn ^{:private true} boot-cljs-build-config-file
  "Given the id of a `boot-cljs` build and a Boot fileset, return the EDN
  file that contains the configuration for the CLJS build."
  [id fs]
  (let [edn-file-name  (str id ".cljs.edn")
        cljs-cfg-files (boot-core/by-name [edn-file-name] (boot-core/input-files fs))]
    (try
      (single! cljs-cfg-files)
      (catch AssertionError e
        (throw
         (ex-info
          (fmtstr "No single boot-cljs configuration file found in fileset; expected one ~s, found ~s"
                  edn-file-name
                  cljs-cfg-files)
          {:expected edn-file-name
           :received cljs-cfg-files}))))))

(defn ^{:private true} boot-cljs-main-js-file
  "Given the id of a `boot-cljs` build and a Boot fileset, returns the
  CLJS-compiled Javascript file that is entry point for the Lambda handler."
  [id fs]
  (let [fs-input-files (boot-core/input-files fs)
        js-file-name   (str id ".js")
        js-file        (single! (boot-core/by-name [js-file-name] fs-input-files))]
    js-file))

(defn ^{:private true} cljs->js:fn-name
  "Given the name of the CLJS function, derive the name under which the
  it will be exported in the project's main Javascript file."
  [x]
  {:pre [(not (nil? x))]}
  (when x (-> x (string/replace #"/" ".") (->snake_case))))

(defn ^{:private true} render-insert
  [spec]
  (stencil/render-file "handler-nodejs-export" spec))

(defn ^{:private true} emit-handler
  [fs id]
  (info (fmtstr "Generating AWS Lambda for build \"~a\"~%" id))
  (let [temp-dir   (boot-core/tmp-dir!)
        _          (boot-core/empty-dir! temp-dir)
        ;; Read the description of the CLJS build that is compiliing the
        ;; handler
        build-spec (read-string
                    (slurp
                     (boot-core/tmp-file
                      (boot-cljs-build-config-file id fs))))
        ;; The build config must include a description of the handler
        ;; specification.
        js-fn-name (or (cljs->js:fn-name
                        (get-in build-spec [handler-key :fn]))
                       (throw
                        (ex-info (fmtstr "No function has been declared as an AWS Lambda handler; please name the function to be exported as the AWS Lambda handler in the configuration for CLJS build ~s using the key ~s"
                                         id
                                         [handler-key :fn])
                                 {:cljs-build {:id id :config build-spec}})))
        ;; Render from our Mustache template the Javascript fragment that
        ;; defines the export.
        js-insert  (render-insert
                    (assoc (get build-spec handler-key)
                           :fn
                           js-fn-name))
        ;; Derive the File object for the module entry point, to which the
        ;; export fragment will be written.
        js-file    (boot-cljs-main-js-file id fs)
        out-file   (io/file temp-dir (boot-core/tmp-path js-file))]
    (info (fmtstr "Exporting ~a as AWS Lambda handler ~s~%"
                  (get-in build-spec [handler-key :fn])
                  js-fn-name))
    (spit out-file (str (slurp (boot-core/tmp-file js-file)) js-insert))
    (-> fs
        (boot-core/rm [js-file])             ; remove defunct .js
        (boot-core/add-resource temp-dir)))) ; add updated .js

(defn handler
  [{:keys [ids]}]
  (boot-core/with-pre-wrap fileset
    (boot-core/commit! (reduce emit-handler fileset ids))))
