FROM openjdk:8-jdk-alpine
RUN apk update && apk add bash
RUN mkdir /app
WORKDIR /app
EXPOSE 8080
COPY target/onedatashare-1.0-SNAPSHOT.jar .
ENTRYPOINT ["java","-jar","/app/onedatashare-1.0-SNAPSHOT.jar"]