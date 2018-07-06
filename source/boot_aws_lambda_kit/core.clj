
(ns boot-aws-lambda-kit.core
  (:require [boot.core]
            [boot-aws-lambda-kit [cljs :as cljs]]))

(defmulti handler-impl ::runtime)

(defmethod handler-impl :clj
  [opts]
  (throw (ex-info "Not implemented"
                  {:ex-type :not-implemented
                   :runtime :clj})))

(defmethod handler-impl :cljs
  [opts]
  (cljs/handler opts))

(defmethod handler-impl :default
  [opts]
  (throw (ex-info "Unsupported runtime"
                  {:ex-type :unsupported-runtime
                   :runtime (::runtime opts)})))

(boot.core/deftask handler:clj
  []
  (handler-impl (merge *opts* ::runtime :cljs)))

(boot.core/deftask handler:cljs
  [i ids SET #{str} "The boot-cljs builds for which to define handlers."]
  (handler-impl (merge *opts* {::runtime :cljs})))
