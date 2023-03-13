package com.springkotlinbatch.common.domain

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.AbstractAggregateRoot
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AbstractAggregationRootEntity<T : AbstractAggregateRoot<T>?> : AbstractAggregateRoot<T>() {
    var deleted: Boolean? = false
        protected set

    @CreatedDate
    var createdAt: LocalDateTime? = LocalDateTime.now()
        protected set

    @CreatedBy
    var createdBy: Long? = null
        protected set

    @LastModifiedDate
    var lastModifiedAt: LocalDateTime? = null
        protected set

    @LastModifiedBy
    var lastModifiedBy: Long? = null
        protected set

    fun delete() {
        this.deleted = true
    }
}
