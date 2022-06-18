FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/config-service-1.0.jar ConfigServer.jar
ENTRYPOINT ["java","-jar","ConfigServer.jar"]


#  docker run -d -p 8888:8888 --network ecommerce-network -e "spring.rabbitmq.host=rabbitmq" --name config-service backtony/config-service:1.0