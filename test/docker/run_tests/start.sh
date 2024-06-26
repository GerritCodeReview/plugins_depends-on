#!/usr/bin/env bash

PORT=29418
TEST_PROJECT=test-project

setup_test_project() {
    echo "Creating a test project ..."
    ssh -p "$PORT" -x "$GERRIT_HOST" gerrit create-project "${TEST_PROJECT}" \
        --owner "Administrators" --submit-type "MERGE_IF_NECESSARY" \
        --empty-commit
    git clone ssh://"$GERRIT_HOST":"$PORT"/"$TEST_PROJECT" "$WORKSPACE"
}

cp -r /depends_on "$USER_HOME"/

cd "$USER_HOME"/depends_on/test
./docker/run_tests/wait-for-it.sh "$GERRIT_HOST":"$PORT" \
    -t 60 -- echo "Gerrit is up"

echo "Update admin account ..."

cat "$USER_HOME"/.ssh/id_rsa.pub | ssh -p 29418 -i /server-ssh-key/ssh_host_rsa_key \
    "Gerrit Code Review@$GERRIT_HOST" suexec --as "admin@example.com" -- \
    gerrit set-account "$USER" --add-ssh-key -

setup_test_project

HTTP_PASSWD=$(uuidgen)
ssh -p 29418 "$GERRIT_HOST" gerrit set-account "$USER" --http-password "$HTTP_PASSWD"
cat <<EOT >> ~/.netrc
machine $GERRIT_HOST
login $USER
password $HTTP_PASSWD
EOT

./test_plugin.sh --server "$GERRIT_HOST" --project "$TEST_PROJECT"
