package com.example.consumer.config

import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerInterceptor
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition

/**
 * These objects are managed by Kafka, not Spring,
 * and so normal Spring dependency injection won’t work for wiring in dependent Spring Beans.
 * https://docs.spring.io/spring-kafka/reference/html/#interceptors
 */
class LoggingConsumerInterceptor : ConsumerInterceptor<String, Any> {

    private val log = KotlinLogging.logger { }

    override fun configure(configs: MutableMap<String, *>) {}

    override fun close() {}

    override fun onCommit(offsets: MutableMap<TopicPartition, OffsetAndMetadata>?) {}

    override fun onConsume(records: ConsumerRecords<String, Any>): ConsumerRecords<String, Any> {
        records.forEach {
            log.info("Start consuming the message: ${it.value()}")
        }
        return records
    }
}
