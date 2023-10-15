FROM openjdk:21
WORKDIR /powerset
COPY target/*.jar /powerset/
COPY scripts/run.sh /powerset/
ENV JAVA_OPTS=""
ENTRYPOINT ["./run.sh"]