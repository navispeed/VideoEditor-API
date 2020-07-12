FROM openjdk:14-slim AS runner

RUN apt-get update
#RUN apt-get install software-properties-common -y
#RUN add-apt-repository ppa:mc3man/trusty-media -y
#RUN apt-get update
RUN apt-get install ffmpeg -y

FROM runner
ARG JAR_FILE
COPY ${JAR_FILE} app.jar


ENTRYPOINT ["java","-jar","/app.jar"]
