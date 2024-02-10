package com.example.producer.publisher

import java.time.LocalDateTime
import java.util.*

data class Article(
    val id: String = UUID.randomUUID().toString(),
    val title: String = UUID.randomUUID().toString(),
    val attachment: List<Attachment> = listOf(Attachment()),
    val registeredDate: LocalDateTime = LocalDateTime.now(),
) {
    data class Attachment(
        val id: String = UUID.randomUUID().toString(),
        val path: String = UUID.randomUUID().toString(),
    )
}


