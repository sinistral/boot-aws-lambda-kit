## boot-aws-lambda-kit

A kit to package Clojure(Script) code as AWS Lambda functions, with a focus on
eliminting boilerplate, rather than on deployment (for which AWS'
CloudFormation, CLI and SDKs all do an admirible job).

## Status

[![Build Status](https://travis-ci.org/sinistral/boot-aws-lambda-kit.svg?branch=master)](https://travis-ci.org/sinistral/boot-aws-lambda-kit)
[![Clojars Project](https://img.shields.io/clojars/v/boot-aws-lambda-kit.svg)](https://clojars.org/boot-aws-lambda-kit)

## Usage

### cljs

Enrich the [boot-cljs] [.cljs.edn] build spec with an entry that describes the
Lambda entry point.  Kit configuration is sought under the key
`:boot-aws-lambda-kit/handler`, with the value a map that defines the Lambda
entry point; the name of the ClojureScript function to be used, and how it
should be exported in the compiled Javascript module. For example (from the
[Hello, Lambda][hello-lambda]  example)

```cljs
{:require [hellolambda.core]
 :boot-aws-lambda-kit/handler {:fn hellolambda.core/main :as handler}
 :compiler-options {:target :nodejs}}
```

Using a Boot task like

```clj
(core/deftask build
  []
  (comp (cljs :ids #{"hellolambda"}
              :optimizations :none
              :compiler-options {:target :nodejs})
        (boot-aws-lambda-kit.core/handler:cljs :ids  #{"hellolambda"})
        (task/jar :file "hellolambda.zip")
        (task/target :dir #{"target"})))
```

will produce output simiar to

```
Compiling ClojureScript...
• hellolambda.js
Generating AWS Lambda for build "hellolambda"
Exporting hellolambda.core/main as AWS Lambda handler hellolambda.handler
Writing hellolambda.zip...
```

and a zip that can be uploaded to S3, or to Lambda directly via the console,
and installed with the handler name `hellolambda.handler`; the CLJS build id
(the name of the .cljs.edn build spec file) defines the Javascript module name,
and the `[:boot-aws-lambda-kit/handler :as]` key the function name.

## Alternatives

<dl>
    <dt>https://github.com/Provisdom/boot-lambda</dt>
    <dd>The code that is at the heart of <tt>boot-aws-lambda-kit</tt> predates
        <tt>boot-lambda</tt>'s <tt>generate-cljs-lambda-index</tt>, making it a
        drop-in solution for other projects that already use this pattern.  For
        new projects, <tt>boot-lambda</tt> is now a comprehensive CLJS
        solution, but <tt>boot-aws-lambda-kit</tt> aspires to cater for both
        Clojure and ClojureScript.
    </dd>
    <dt>https://github.com/mhjort/clj-lambda-utils</dt>
    <dd>Primarily for the deployment of Lambda functions</dd>
</dl>

## License

Published under the [2-clause BSD license][license].

[boot-cljs]: https://github.com/boot-clj/boot-cljs
[.cljs.edn]: https://github.com/boot-clj/boot-cljs/blob/master/docs/cljs.edn.md
[hello-lambda]: https://github.com/sinistral/hellolambda
[license]: https://opensource.org/licenses/BSD-2-Clause
