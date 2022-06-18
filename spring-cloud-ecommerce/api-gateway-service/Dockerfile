FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/api-gateway-service-1.0.jar ApiGatewayServer.jar
ENTRYPOINT ["java","-jar","ApiGatewayServer.jar"]

#docker run -d -p 8000:8000 --network ecommerce-network \
#-e "spring.cloud.config.uri=http://config-service:8888" \
#-e "spring.rabbitmq.host=rabbitmq" \
#-e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" \
#--name api-gateway-service backtony/api-gateway-service:1.0