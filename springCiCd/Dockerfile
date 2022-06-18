FROM openjdk:11-jdk

VOLUME /tmp

ARG JAR_FILE=./build/libs/awsstudy-1.0.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","app.jar"]