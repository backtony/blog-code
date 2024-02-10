package com.example.producer.listener

import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.ssl.SslBundles
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.MicrometerConsumerListener
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.support.serializer.JsonDeserializer

@EnableKafka
@Configuration
class ConsumerConfig(
    private val kafkaProperties: KafkaProperties,
    private val meterRegistry: MeterRegistry,
    private val sslBundles: SslBundles,
) {

    @Bean(COMMON)
    fun commonKafkaListenerContainerFactory(
        commonConsumerFactory: ConsumerFactory<String, Any>,
    ): KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Any>> {
        return ConcurrentKafkaListenerContainerFactory<String, Any>().apply {
            consumerFactory = commonConsumerFactory
        }
    }

    @Bean
    fun commonConsumerFactory(): ConsumerFactory<String, Any> {
        val keyDeserializer = StringDeserializer()
        val valueDeserializer = JsonDeserializer(Any::class.java).apply {
            addTrustedPackages("com.example.*")
        }

        return DefaultKafkaConsumerFactory(getCommonConsumerConfigs(), keyDeserializer, valueDeserializer)
            .apply { addListener(MicrometerConsumerListener(meterRegistry)) }
    }

    private fun getCommonConsumerConfigs(): Map<String, Any> {
        return kafkaProperties.buildConsumerProperties(sslBundles)
    }

    companion object {
        const val COMMON = "commonKafkaListenerContainerFactory"
    }
}
