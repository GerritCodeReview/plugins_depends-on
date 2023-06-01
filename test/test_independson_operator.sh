#!/usr/bin/env bash

# This test relies on change operator aliasing for "in_depends-on" operator
# since query parser is not able to parse dash(-) in depends-on
# operator.
# This test assumes that change operator aliasing for "in_depends-on" operator
# is configured as follows :
#
#  [operator-alias "change"]
#      independson = in_depends-on

# run a gerrit ssh command
gssh() { ssh -x -p "$PORT" "$SERVER" "$@" ; 2>&1 ; } # [args]...

query() { gssh gerrit query --format=json "$@" | head -1 ; }

q() { "$@" > /dev/null 2>&1 ; } # cmd [args...]  # quiet a command

die() { echo -e "$@" ; exit 1 ; } # error_message

mygit() { git -C "$REPO_DIR" "$@" ; } # [args...]

# > uuid
gen_uuid() { uuidgen | sha1sum | awk '{print $1}' ; }

gen_commit_msg() { # msg > commit_msg
    local msg=$1
    echo "$msg

Change-Id: I$(gen_uuid)"
}

get_change_num() { # < gerrit_push_response > changenum
    local url=$(awk '$NF ~ /\[NEW\]/ { print $2 }')
    echo "${url##*\/}" | tr -d -c '[:digit:]'
}

create_change() { # branch file [commit_message] > changenum
    local branch=$1 tmpfile=$2 msg=$3 out rtn
    local content=$RANDOM dest=refs/for/$branch

    out=$(mygit fetch "$GITURL" "$branch" 2>&1) ||\
       die "Failed to fetch $branch: $out"
    out=$(mygit checkout FETCH_HEAD 2>&1) ||\
       die "Failed to checkout $branch: $out"

    echo -e "$content" > "$tmpfile"

    out=$(mygit add "$tmpfile" 2>&1) || die "Failed to git add: $out"

    msg=$(gen_commit_msg "Add $tmpfile")

    out=$(mygit commit -m "$msg" 2>&1) ||\
        die "Failed to commit change: $out"
    [ -n "$VERBOSE" ] && echo "  commit:$out" >&2

    out=$(mygit push "$GITURL" "HEAD:$dest" 2>&1) ||\
        die "Failed to push change: $out"
    out=$(echo "$out" | get_change_num) ; rtn=$? ; echo "$out"
    [ -n "$VERBOSE" ] && echo "  change:$out" >&2
    return $rtn
}

# ------------------------- Usage ---------------------------

usage() { # [error_message]
    cat <<-EOF
Usage: $MYPROG [-s|--server <server>] [-p|--project <project>]
             [-r|--srcref <ref branch>] [-h|--help]

       -h|--help                 usage/help
       -s|--server <server>      server to use for the test (default: localhost)
       -p|--project <project>    git project to use (default: project0)
       -r|--srcref <ref branch>  reference branch used to create changes (default: master)
EOF

    [ -n "$1" ] && echo -e '\n'"ERROR: $1"
    exit 1
}

parseArgs() {
    SERVER="localhost"
    PROJECT="tools/test/project0"
    SRC_REF_BRANCH="master"
    while (( "$#" )) ; do
        case "$1" in
            --server|-s)  shift; SERVER=$1 ;;
            --project|-p) shift; PROJECT=$1 ;;
            --srcref|-r)  shift; SRC_REF_BRANCH=$1 ;;
            --help|-h)    usage ;;
            --verbose|-v) VERBOSE=$1 ;;
            *)            usage "invalid argument '$1'" ;;
        esac
        shift
    done

    [ -n "$SERVER" ]     || usage "server not set"
    [ -n "$PROJECT" ]    || usage "project not set"
    [ -n "$SRC_REF_BRANCH" ] || usage "source ref branch not set"
}

MYPROG=$(basename "$0")
MYDIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

source "$MYDIR/lib_result.sh"
PORT=29418

parseArgs "$@"

TEST_DIR="$MYDIR/../target/test"
rm -rf "$TEST_DIR"
mkdir -p "$TEST_DIR"

GITURL=ssh://"$SERVER:$PORT/$PROJECT"

SRC_REF="$SRC_REF_BRANCH"
echo "$SRC_REF_BRANCH" | grep -q '^refs/' || SRC_REF=refs/heads/"$SRC_REF_BRANCH"

REPO_DIR="$TEST_DIR/"repo
q git init "$REPO_DIR"
FILE_A="$REPO_DIR"/fileA

# ------------------------- Depends-on Test ---------------------------
DEPENDENT_CHANGE=$(create_change "$SRC_REF_BRANCH" "$FILE_A") || \
    die "Failed to create change on project: $PROJECT branch: $SRC_REF_BRANCH"
CHANGE=$(create_change "$SRC_REF_BRANCH" "$FILE_A") || \
    die "Failed to create change on project: $PROJECT branch: $SRC_REF_BRANCH"
gssh gerrit review --message \'"Depends-on: $DEPENDENT_CHANGE"\' "$CHANGE",1
EXPECTED="$(query "$DEPENDENT_CHANGE" | jq --raw-output '.number')"
ACTUAL="$(query "independson:$CHANGE" | jq --raw-output '.number')"
result_out "independson operator" "$EXPECTED" "$ACTUAL"
exit $RESULT
