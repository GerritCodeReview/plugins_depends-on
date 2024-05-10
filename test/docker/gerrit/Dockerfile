FROM gerritcodereview/gerrit:3.9.1-ubuntu22

ENV GERRIT_SITE /var/gerrit
RUN rm -rf "$GERRIT_SITE/plugins" && mkdir "$GERRIT_SITE/plugins"
COPY artifacts/plugins/ $GERRIT_SITE/plugins/

USER root

COPY artifacts/bin/ /tmp/
COPY start.sh /
RUN { [ -e /tmp/gerrit.war ] && cp /tmp/gerrit.war "$GERRIT_SITE/bin/gerrit.war" ; } || true

USER gerrit
ENTRYPOINT /start.sh
