#!/usr/bin/env bash

# run a gerrit ssh command
gssh() { ssh -x -p "$PORT" "$SERVER" "$@" ; 2>&1 ; } # [args]...

query() {
    gssh gerrit query --format=json "$@" | head -1 | \
        python -c 'import sys,json; print json.dumps(json.load(sys.stdin))'
}

q() { "$@" > /dev/null 2>&1 ; } # cmd [args...]  # quiet a command

die() { echo -e "$@" ; exit 1 ; } # error_message

mygit() { git --work-tree="$REPO_DIR" --git-dir="$GIT_DIR" "$@" ; } # [args...]

# > uuid
gen_uuid() { uuidgen | openssl dgst -sha1 -binary | xxd -p; }

gen_commit_msg() { # msg > commit_msg
    local msg=$1
    echo "$msg

Change-Id: I$(gen_uuid)"
}

get_open_changes() {
    curl --netrc --silent "http://$SERVER:8080/a/changes/?q=status:open"
}

get_branch_revision() { # prj branch > revision
    curl --netrc --silent \
        "http://$SERVER:8080/a/projects/$1/branches/$2" | \
        tail -n +2 | jq --raw-output '.revision'
}

create_branch() { # prj revision dest_branch
    curl --netrc --silent --data "revision=$2" \
        "http://$SERVER:8080/a/projects/$1/branches/$3"
}

get_change_num() { # < gerrit_push_response > changenum
    local url=$(awk '$NF ~ /\[NEW\]/ { print $2 }')
    echo "${url##*\/}" | tr -d -c '[:digit:]'
}

cherry_pick_change() { # change_num dest_branch > changenum
    curl -X POST --netrc --silent --header 'Content-Type: application/json' \
        --data '{"message" : "Copied Change", "destination" : "'$2'"}' \
        "http://$SERVER:8080/a/changes/$1/revisions/current/cherrypick" | \
        tail -n +2 | jq --raw-output '._number'
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

get_depends_on_tag() { # change > depends-on tag
    local change_number=$1
    IFS=$'\n'
    local comments=( $(gssh gerrit query --comments "$change_number") )
    IFS=''
    for ((i = "${#comments[@]}" -1 ; i >= 0 ; i--)) ; do
        if [[ "${comments[i]}" =~ 'Depends-on:' ]] ; then
            echo "${comments[i]}" | sed 's/^ *//g'
            break
        fi
    done
}

# ------------------------- Usage ---------------------------

usage() { # [error_message]
    cat <<-EOF
Usage: $MYPROG [-s|--server <server>] [-p|--project <project>]
             [-r|--srcref <ref branch>] [-d|--destref <ref branch>] [-h|--help]

       -h|--help                 usage/help
       -s|--server <server>      server to use for the test (default: localhost)
       -p|--project <project>    git project to use (default: project0)
       -r|--srcref <ref branch>  reference branch used to create changes (default: master)
       -d|--destref <ref branch> reference branch used to propagate change (default: foo)
EOF

    [ -n "$1" ] && echo -e '\n'"ERROR: $1"
    exit 1
}

parseArgs() {
    SERVER="localhost"
    PROJECT="tools/test/project0"
    SRC_REF_BRANCH="master"
    DEST_REF_BRANCH="foo"
    while (( "$#" )) ; do
        case "$1" in
            --server|-s)  shift; SERVER=$1 ;;
            --project|-p) shift; PROJECT=$1 ;;
            --srcref|-r)  shift; SRC_REF_BRANCH=$1 ;;
            --destref|-d) shift; DEST_REF_BRANCH=$1 ;;
            --help|-h)    usage ;;
            --verbose|-v) VERBOSE=$1 ;;
            *)            usage "invalid argument '$1'" ;;
        esac
        shift
    done

    [ -n "$SERVER" ]     || usage "server not set"
    [ -n "$PROJECT" ]    || usage "project not set"
    [ -n "$SRC_REF_BRANCH" ] || usage "source ref branch not set"
    [ -n "$DEST_REF_BRANCH" ] || usage "dest ref branch not set"
}

MYPROG=$(basename "$0")
MYDIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"

source "$MYDIR/lib_result.sh"
PORT=29418

parseArgs "$@"

TEST_DIR="$MYDIR/../target/test"
rm -rf "$TEST_DIR"
mkdir -p "$TEST_DIR"

GITURL=ssh://$SERVER:$PORT/$PROJECT

# We need to do an initial REST call, as the first REST call after a server is
# brought up results in being anonymous despite providing proper authentication.
q get_open_changes

q create_branch "$PROJECT" "$(get_branch_revision "$PROJECT" "$SRC_REF_BRANCH")" "$DEST_REF_BRANCH"

SRC_REF=$SRC_REF_BRANCH
echo "$SRC_REF_BRANCH" | grep -q '^refs/' || SRC_REF=refs/heads/$SRC_REF_BRANCH
git ls-remote "$GITURL" | grep -q "$SRC_REF" || usage "invalid project/server/srcref"
DEST_REF=$DEST_REF_BRANCH
echo "$DEST_REF_BRANCH" | grep -q '^refs/' || DEST_REF=refs/heads/$DEST_REF_BRANCH
git ls-remote "$GITURL" | grep -q "$DEST_REF" || usage "invalid project/server/destref"

REPO_DIR=$TEST_DIR/repo
q git init "$REPO_DIR"
GIT_DIR=$REPO_DIR/.git
FILE_A=$REPO_DIR/fileA

# ------------------------- Depends-on Test ---------------------------
base_change=$(create_change "$SRC_REF_BRANCH" "$FILE_A") || exit
src_change=$(create_change "$SRC_REF_BRANCH" "$FILE_A") || exit
gssh gerrit review --message \'"Depends-on: $base_change"\' "$src_change",1
dest_change=$(cherry_pick_change "$src_change" "$DEST_REF")
expected="Depends-on: $(query "$base_change" | jq --raw-output '.id')"
actual=$(get_depends_on_tag "$dest_change")
result_out "propagate depends-on" "$expected" "$actual"

exit $RESULT
