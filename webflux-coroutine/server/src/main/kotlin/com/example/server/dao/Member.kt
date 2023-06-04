package com.example.server.dao

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table
data class Member(
    @Id
    @Column("member_id")
    val id: Long? = null,
    var name: String,
    var teamId: Long? = null,
    @CreatedDate
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @LastModifiedDate
    val modifiedAt: LocalDateTime = LocalDateTime.now()
) {

    // ManyToOne
    // lateinit 을 사용할 경우, save할 때 init이 안되어 있으면 에러 발생하므로 lateinit 제거
    @Transient
    var team: Team? = null
}
