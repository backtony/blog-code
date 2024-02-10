package com.example.consumer.config

import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.listener.ConsumerRecordRecoverer
import org.springframework.kafka.listener.ListenerExecutionFailedException
import org.springframework.stereotype.Component

@Component
class CommonConsumerRecordRecoverer : ConsumerRecordRecoverer {

    private val log = KotlinLogging.logger { }

    override fun accept(record: ConsumerRecord<*, *>, ex: Exception) {

        var groupId: String? = ""
        if (ex is ListenerExecutionFailedException) {
            groupId = ex.groupId
        }

        log.error(
            "[Consumer error] occurred error while consuming message. " +
                "topic : ${record.topic()}, groupId : $groupId, offset : ${record.offset()}, " +
                "key : ${record.key()}, value : ${record.value()}, error message : ${ex.message}",
            ex,
        )
    }
}
