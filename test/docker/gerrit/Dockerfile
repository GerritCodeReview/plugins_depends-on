FROM gerritcodereview/gerrit:3.2.10-ubuntu20

USER root

ENV GERRIT_SITE /var/gerrit
RUN git config -f "$GERRIT_SITE/etc/gerrit.config" auth.type \
    DEVELOPMENT_BECOME_ANY_ACCOUNT

COPY artifacts /tmp/
RUN cp /tmp/depends-on.jar "$GERRIT_SITE/plugins/depends-on.jar"
RUN { [ -e /tmp/gerrit.war ] && cp /tmp/gerrit.war "$GERRIT_SITE/bin/gerrit.war" ; } || true

USER gerrit
