package com.example.producer.publisher

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SamplePublisher(
    private val commonKafkaTemplate: KafkaTemplate<String, Any>,
) {

    @PostMapping("/articles")
    fun publishMessage() {
        val messages = mutableListOf<KafkaMessage>()
        repeat(2) {
            val article = Article()
            messages.add(KafkaMessage(
                topic = "backtony-test",
                key = article.id,
                data = article,
            ))
        }

        for (message in messages) {
            commonKafkaTemplate.send(message.buildProducerRecord())
        }
    }
}
