FROM ghcr.io/ministryofjustice/laa-base-container-images/java:25-corretto-alpine

EXPOSE 9000
ARG JAR_FILE=build/libs/app.jar
COPY ${JAR_FILE} /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
