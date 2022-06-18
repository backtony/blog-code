FROM openjdk:11-jdk

VOLUME /tmp

ARG JAR_FILE=./build/libs/test-1.0.jar

COPY ${JAR_FILE} app.jar

# 운영
ENTRYPOINT ["nohup","java","-jar",\
"-javaagent:./pinpoint/pinpoint-bootstrap-2.2.2.jar",\
"-Dpinpoint.agentId=gjgs01","-Dpinpoint.applicationName=gjgs",\
"-Dpinpoint.config=./pinpoint/pinpoint-root.config"\
,"-Dspring.profiles.active=prod","app.jar","2>&1","&"]

#ENTRYPOINT ["nohup","java","-jar","-Dspring.profiles.active=prod","app.jar","2>&1","&"]
