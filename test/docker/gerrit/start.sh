#!/usr/bin/env bash

git config -f "$GERRIT_SITE/etc/gerrit.config" \
    operator-alias.change.independson "in_depends-on"
git config -f "$GERRIT_SITE/etc/gerrit.config" \
    operator-alias.change.hasdependson "has_depends-on"
git config -f "$GERRIT_SITE/etc/gerrit.config" \
    sendemail.enable "false"

echo "Initializing Gerrit site ..."
java -jar "$GERRIT_SITE/bin/gerrit.war" init -d "$GERRIT_SITE" --batch

echo "Running Gerrit ..."
exec "$GERRIT_SITE"/bin/gerrit.sh run