package com.example.producer.publisher

import org.apache.kafka.clients.producer.ProducerRecord

data class KafkaMessage(
    val topic: String,
    val key: String,
    val data: Any,
    val headers: MutableMap<String, String> = mutableMapOf(),

    // 이벤트 발행과정에서 DB Save 후, consumer에서 published true로 변경하는 구조에서 사용하는 필드
//    var id: String? = null,
//    val type: String,
//    var published: Boolean = false,
//    val registeredDate: LocalDateTime,
) {
    fun buildProducerRecord(): ProducerRecord<String, Any> {
        return ProducerRecord(topic, key, data).apply {
            headers.entries.forEach {
                this.headers().add(it.key, it.value.toByteArray())
            }
        }
    }
}
