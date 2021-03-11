FROM adoptopenjdk/openjdk11:alpine-jre
RUN mkdir -p /opt/imast/worker


COPY target/worker-jar-with-dependencies.jar /opt/imast/worker

ENTRYPOINT exec java $JAVA_OPTS -jar /opt/imast/worker/worker-jar-with-dependencies.jar

