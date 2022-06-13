#!/usr/bin/env bash

readlink -f / &> /dev/null || readlink() { greadlink "$@" ; } # for MacOS
CUR_DIR=$(dirname -- "$(readlink -f -- "$0")")

RESULT=0
"$CUR_DIR"/test_dependson.sh "$@" || RESULT=1
"$CUR_DIR"/test_dependson_operators.sh "$@" || RESULT=1

exit $RESULT
