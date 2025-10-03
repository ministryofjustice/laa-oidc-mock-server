FROM eclipse-temurin:21
EXPOSE 9000
ARG JAR_FILE=build/libs/laa-oidc-mock-server-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]