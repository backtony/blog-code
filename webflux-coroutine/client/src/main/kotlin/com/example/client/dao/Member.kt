package com.example.server.dao

import java.time.LocalDateTime

data class Member(
    val id: Long? = null,
    var name: String,
    var teamId: Long? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val modifiedAt: LocalDateTime = LocalDateTime.now()
) {

    // ManyToOne
    var team: Team? = null
}
