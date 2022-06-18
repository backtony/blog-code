FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/catalog-service-1.0.jar CatalogServer.jar
ENTRYPOINT ["java","-jar","CatalogServer.jar"]

#docker run -d --network ecommerce-network \
#--name catalog-service \
#-e "spring.cloud.config.uri=http://config-service:8888" \
#-e "spring.rabbitmq.host=rabbitmq" \
#-e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" \
#backtony/catalog-service:1.0
