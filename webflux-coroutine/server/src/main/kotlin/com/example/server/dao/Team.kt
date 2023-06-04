package com.example.server.dao

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

// team 과 member는 oneToMany 관계라고 가정
@Table
data class Team(
    @Id
    @Column("team_id")
    val id: Long? = null,
    var name: String,
    var leaderId: Long,
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @LastModifiedDate
    val modifiedAt: LocalDateTime = LocalDateTime.now()
) {

    // OneToOne
    // lateinit 을 사용할 경우, save할 때 init이 안되어 있으면 에러 발생하므로 lateinit 제거
    @Transient
    var leader: Member? = null

    // OneToMany
    // lateinit 을 사용할 경우, save할 때 init이 안되어 있으면 에러 발생하므로 lateinit 제거
    @Transient
    var members: List<Member> = emptyList()
}
