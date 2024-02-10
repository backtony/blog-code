package com.example.producer.listener

import com.example.producer.listener.ConsumerConfig.Companion.COMMON
import com.example.producer.publisher.Article
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.KafkaListener

@Configuration
class SampleListener {

    private val log = KotlinLogging.logger { }

    @KafkaListener(
        groupId = "backtony-test-single",
        topics = ["backtony-test"],
        containerFactory = COMMON,
        concurrency = "3"
    )
    fun sample(record: Article) {
        log.info { record }
    }

    @KafkaListener(
        groupId = "backtony-test-batch",
        topics = ["backtony-test"],
        containerFactory = COMMON,
        batch = "true",
    )
    fun sampleBatch(event: List<Article>) {
        log.info { "batch count : ${event.size}" }
    }
}
