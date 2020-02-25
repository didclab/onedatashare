# Create a container from the latest Maven image (Maven images are coupled with JDK)
FROM openjdk:8-jdk-alpine
RUN apk update && apk add bash
COPY target/onedatashare-1.0-SNAPSHOT.jar .
# Command to run whenever the container is started
ENTRYPOINT ["java","-jar","onedatashare-1.0-SNAPSHOT.jar"]