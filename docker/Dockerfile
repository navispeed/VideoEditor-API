FROM openjdk:14-slim

ARG JAR_FILE
COPY ${JAR_FILE} app.jar

RUN apt-get install ffmpeg libavcodec-extra-53

ENTRYPOINT ["java","-jar","/app.jar"]
