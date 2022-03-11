#!/bin/bash

repl(){
  clj \
    -J-Dclojure.core.async.pool-size=1 \
    -X:repl Ripley.core/process \
    :main-ns gunslinger.main
}


main(){
  clojure \
    -J-Dclojure.core.async.pool-size=1 \
    -M -m gunslinger.main
}

jar(){

  clojure \
    -X:identicon Zazu.core/process \
    :word '"gunslinger"' \
    :filename '"out/identicon/icon.png"' \
    :size 256

  rm -rf out/*.jar
  clojure \
    -X:uberjar Genie.core/process \
    :main-ns gunslinger.main \
    :filename "\"out/gunslinger-$(git rev-parse --short HEAD).jar\"" \
    :paths '["src"]'
}

release(){
  jar
}

"$@"