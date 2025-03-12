FROM openjdk:11
VOLUME /tmp
COPY target/ms-transactions-0.0.1-SNAPSHOT.jar java-app.jar
ENTRYPOINT ["java","-jar","java-app.jar"]