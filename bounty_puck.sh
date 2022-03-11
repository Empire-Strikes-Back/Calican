#!/bin/bash

repl(){
  clj \
    -J-Dclojure.core.async.pool-size=1 \
    -X:repl Ripley.core/process \
    :main-ns lingonberry.main
}


main(){
  clojure \
    -J-Dclojure.core.async.pool-size=1 \
    -M -m lingonberry.main
}

jar(){

  clojure \
    -X:identicon Zazu.core/process \
    :word '"lingonberry"' \
    :filename '"out/identicon/icon.png"' \
    :size 256

  rm -rf out/*.jar
  clojure \
    -X:uberjar Genie.core/process \
    :main-ns lingonberry.main \
    :filename "\"out/lingonberry-$(git rev-parse --short HEAD).jar\"" \
    :paths '["src"]'
}

release(){
  jar
}

"$@"