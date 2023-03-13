package com.springkotlinbatch.common.domain

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    var deleted: Boolean? = false
        protected set

    @CreatedDate
    var createDate: LocalDateTime? = LocalDateTime.now()
        protected set

    @CreatedBy
    var createdBy: Long? = null
        protected set

    @LastModifiedDate
    var lastModifiedDate: LocalDateTime? = null
        protected set

    @LastModifiedBy
    var lastModifiedBy: Long? = null
        protected set
}
