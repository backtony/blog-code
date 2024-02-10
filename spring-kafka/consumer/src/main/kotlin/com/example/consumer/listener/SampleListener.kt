package com.example.consumer.listener

import com.example.consumer.config.ConsumerConfig.Companion.COMMON
import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload

@Configuration
class SampleListener {

    private val log = KotlinLogging.logger { }

    @KafkaListener(
        groupId = "backtony-test-single",
        topics = ["backtony-test"],
        containerFactory = COMMON,
    )
    fun sample(record: ConsumerRecord<String, Article>) {
        log.info { record.value() }
    }

    @KafkaListener(
        groupId = "backtony-test-batch",
        topics = ["backtony-test"],
        containerFactory = COMMON,
        batch = "true",
    )
    fun sampleBatch(event: List<ConsumerRecord<String, Article>>) {
        log.info { "batch count : ${event.size}" }
    }
}
