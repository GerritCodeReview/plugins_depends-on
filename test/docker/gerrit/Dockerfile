FROM gerritcodereview/gerrit:3.9.1-ubuntu22

ENV GERRIT_SITE /var/gerrit
RUN rm -rf "$GERRIT_SITE/plugins" && mkdir "$GERRIT_SITE/plugins"
RUN git config -f "$GERRIT_SITE/etc/gerrit.config" auth.type \
    DEVELOPMENT_BECOME_ANY_ACCOUNT
RUN git config -f "$GERRIT_SITE/etc/gerrit.config" \
    sendemail.enable "false"
RUN git config -f "$GERRIT_SITE/etc/gerrit.config" \
    "operator-alias.change.independson" "in_depends-on"
RUN git config -f "$GERRIT_SITE/etc/gerrit.config" \
    "operator-alias.change.hasdependson" "has_depends-on"

COPY artifacts /tmp/
RUN cp /tmp/plugins/* "$GERRIT_SITE/plugins/"
RUN { [ -e /tmp/bin/gerrit.war ] && cp /tmp/bin/gerrit.war "$GERRIT_SITE/bin/gerrit.war" ; } || true
RUN chmod 777 "$GERRIT_SITE/git"
