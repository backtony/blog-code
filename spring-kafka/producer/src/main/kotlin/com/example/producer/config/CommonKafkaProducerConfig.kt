package com.example.producer.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.ssl.SslBundles
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.MicrometerProducerListener
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.ProducerListener
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class CommonKafkaProducerConfig(
    private val kafkaProperties: KafkaProperties,
    private val meterRegistry: MeterRegistry,
    private val sslBundles: SslBundles,
) {

    @Bean
    fun commonKafkaTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(commonProducerFactory()).apply {
            setProducerListener(CommonKafkaListener())
        }
    }

    @Bean
    fun commonProducerFactory(): ProducerFactory<String, Any> {
        val keySerializer = StringSerializer()
        val valueSerializer = JsonSerializer<Any>()

        return DefaultKafkaProducerFactory(kafkaProperties.buildProducerProperties(sslBundles), keySerializer, valueSerializer)
            .apply { addListener(MicrometerProducerListener(meterRegistry)) }
    }
}

class CommonKafkaListener : ProducerListener<String, Any> {

    private val log = KotlinLogging.logger { }

    override fun onError(producerRecord: ProducerRecord<String, Any>, recordMetadata: RecordMetadata?, exception: java.lang.Exception?) {
        log.error(
            "Fail to send kafka Message. Topic: ${producerRecord.topic()}, Partition: ${producerRecord.partition()}," +
                " Key: ${producerRecord.key()},  Value: ${producerRecord.value()}",
            exception,
        )
    }
}
