# ---- TEST RESULTS ----

RESULT=0

result() { # test [error_message]
    local result=$?
    local outcome="FAIL"
    if [ $result -eq 0 ] ; then
        echo "PASSED - $1 test"
        outcome="PASS"
    else
        echo "*** FAILED *** - $1 test"
        RESULT=$result
        [ $# -gt 1 ] && echo "$2"
    fi
    [ -n "$RESULT_CALLBACK" ] &&
        "$RESULT_CALLBACK" "$(basename "$0")" "$1" "$outcome"
}

# output must match expected to pass
result_out() { # test expected output
    local disp=$(echo "Expected Output:" ;\
                 echo "    $2" ;\
                 echo "Actual Output:" ;\
                 echo "    $3")

    [ "$2" = "$3" ]
    result "$1" "$disp"
}

# output must not match unallowed to pass
result_not_out() { # test unallowed output
    local disp=$(echo "Unallowed Output:" ;\
                 echo "    $2" ;\
                 echo "Actual Output:" ;\
                 echo "    $3")

    [ "$2" != "$3" ]
    result "$1" "$disp"
}
