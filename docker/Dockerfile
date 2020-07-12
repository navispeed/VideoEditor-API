FROM jrottenberg/ffmpeg:4.1-alpine

ARG JAR_FILE
COPY ${JAR_FILE} app.jar

RUN apt-get update
#RUN apt-get install software-properties-common -y
#RUN add-apt-repository ppa:mc3man/trusty-media -y
#RUN apt-get update
RUN apt-get install openjdk-14-jre

ENTRYPOINT ["java","-jar","/app.jar"]
