FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/user-service-1.0.jar UserServer.jar
ENTRYPOINT ["java","-jar","UserServer.jar"]

#docker run -d --network ecommerce-network \
#--name user-service \
#-e "spring.cloud.config.uri=http://config-service:8888" \
#-e "spring.rabbitmq.host=rabbitmq" \
#-e "spring.zipkin.base-url=http://zipkin:9411" \
#-e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" \
#backtony/user-service:1.0