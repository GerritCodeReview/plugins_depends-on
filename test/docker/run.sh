#!/usr/bin/env bash

readlink --canonicalize / &> /dev/null || readlink() { greadlink "$@" ; } # for MacOS
MYDIR=$(dirname -- "$(readlink -f -- "$0")")
ARTIFACTS=$MYDIR/gerrit/artifacts
BAZEL_BUILT_JAR=$MYDIR/../../bazel-bin/depends-on.jar

die() { echo -e "\nERROR: $@" ; kill $$ ; exit 1 ; } # error_message

progress() { # message cmd [args]...
    local message=$1 ; shift
    echo -n "$message"
    "$@" &
    local pid=$!
    while kill -0 $pid 2> /dev/null ; do
        echo -n "."
        sleep 2
    done
    echo
    wait "$pid"
}

usage() { # [error_message]
    local prog=$(basename "$0")
    cat <<EOF
Usage:
    $prog [--depends-on-plugin-jar|-t <FILE_PATH>] [--gerrit-war|-g <FILE_PATH>]

    This tool runs the plugin functional tests in a Docker environment built
    from the gerritcodereview/gerrit base Docker image.

    Options:
    --help|-h
    --gerrit-war|-g                 optional path to Gerrit WAR file. Will
                                    likely not function correctly if it's a
                                    different MAJOR.MINOR version than the
                                    default version in
                                    test/docker/gerrit/Dockerfile.
    --depends-on-plugin-jar|-e      optional path to depends-on plugin JAR file.
                                    Defaults to $BAZEL_BUILT_JAR

EOF

    [ -n "$1" ] && echo -e "\nERROR: $1" && exit 1
    exit 0
}

check_prerequisite() {
    docker --version > /dev/null || die "docker is not installed"
    docker-compose --version > /dev/null || die "docker-compose is not installed"
}

build_images() {
    docker-compose "${COMPOSE_ARGS[@]}" build --quiet
}

run_depends_on_plugin_tests() {
    docker-compose "${COMPOSE_ARGS[@]}" up --detach
    docker-compose "${COMPOSE_ARGS[@]}" exec -T --user=gerrit_admin run_tests \
        '/depends_on/test/docker/run_tests/start.sh'
}

cleanup() {
    docker-compose "${COMPOSE_ARGS[@]}" down -v --rmi local 2>/dev/null
    rm -rf "$ARTIFACTS"
}

while (( "$#" )); do
    case "$1" in
        --help|-h)                    usage ;;
        --gerrit-war|-g)              shift ; GERRIT_WAR=$1 ;;
        --depends-on-plugin-jar|-e)   shift ; DEPENDS_ON_PLUGIN_JAR=$1 ;;
        *)                            usage "invalid argument $1" ;;
    esac
    shift
done

PROJECT_NAME="depends_on_$$"
COMPOSE_YAML="$MYDIR/docker-compose.yaml"
COMPOSE_ARGS=(--project-name "$PROJECT_NAME" -f "$COMPOSE_YAML")
check_prerequisite
mkdir -p -- "$ARTIFACTS"
if [ -n "$DEPENDS_ON_PLUGIN_JAR" ] ; then
    cp -f "$DEPENDS_ON_PLUGIN_JAR" "$ARTIFACTS/depends-on.jar"
elif [ -e "$BAZEL_BUILT_JAR" ] ; then
    cp -f "$BAZEL_BUILT_JAR" "$ARTIFACTS/depends-on.jar"
else
    usage "Cannot find plugin jar, did you forget --depends-on-plugin-jar?"
fi
[ -n "$GERRIT_WAR" ] && cp -f "$GERRIT_WAR" "$ARTIFACTS/gerrit.war"
( trap cleanup EXIT SIGTERM
    progress "Building docker images" build_images
    run_depends_on_plugin_tests
)
