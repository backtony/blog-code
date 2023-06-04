package com.example.server.dao

import java.time.LocalDateTime

// team 과 member는 oneToMany 관계라고 가정
data class Team(
    val id: Long? = null,
    var name: String,
    var leaderId: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val modifiedAt: LocalDateTime = LocalDateTime.now()
) {

    // OneToOne
    var leader: Member? = null

    // OneToMany
    var members: List<Member> = emptyList()
}
