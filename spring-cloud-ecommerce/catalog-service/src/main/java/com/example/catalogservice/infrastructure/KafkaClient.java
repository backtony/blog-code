package com.example.catalogservice.infrastructure;

import com.example.catalogservice.domain.Catalog;
import com.example.catalogservice.domain.CatalogRepository;
import com.example.catalogservice.domain.service.MessageQueueClient;
import com.example.catalogservice.infrastructure.dto.OrderDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class KafkaClient implements MessageQueueClient {

    private final CatalogRepository catalogRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "example-catalog-topic")
    @Override
    public void updateQty(String message) {
        log.info("kafka message: -> {}",message);

        OrderDto orderDto = null;
        try {
            orderDto = objectMapper.readValue(message, OrderDto.class);
        } catch (JsonProcessingException e){
            e.printStackTrace();
        }

        Catalog catalog = catalogRepository.findByProductId(orderDto.getProductId())
                .orElseThrow(() -> new RuntimeException("카탈로그기 존재하지 않음"));
        catalog.minusStock(orderDto.getQty());
    }
}
