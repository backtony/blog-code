package com.example.orderservice.infrastructure;

import com.example.orderservice.domain.service.MessageQueueClient;
import com.example.orderservice.domain.service.dto.response.OrderInfoResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer implements MessageQueueClient {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper;

    public void send(String topic, OrderInfoResponseDto orderInfoResponseDto){
        String json = null;
        try {
            json = mapper.writeValueAsString(orderInfoResponseDto);
        } catch (JsonProcessingException e){
            e.printStackTrace();
        }

        kafkaTemplate.send(topic,json);
        log.info("kafka producer send data from the order microService: {}",orderInfoResponseDto);
    }

}
