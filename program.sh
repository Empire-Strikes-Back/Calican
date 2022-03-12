#!/bin/bash

repl(){
  clj \
    -J-Dclojure.core.async.pool-size=1 \
    -X:repl Ripley.core/process \
    :main-ns Calican.main
}


main(){
  clojure \
    -J-Dclojure.core.async.pool-size=1 \
    -M -m Calican.main
}

jar(){

  clojure \
    -X:identicon Zazu.core/process \
    :word '"Calican"' \
    :filename '"out/identicon/icon.png"' \
    :size 256

  rm -rf out/*.jar
  clojure \
    -X:uberjar Genie.core/process \
    :main-ns Calican.main \
    :filename "\"out/Calican-$(git rev-parse --short HEAD).jar\"" \
    :paths '["src"]'
}

release(){
  jar
}

"$@"