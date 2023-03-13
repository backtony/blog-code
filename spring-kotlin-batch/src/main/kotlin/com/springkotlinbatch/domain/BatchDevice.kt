package com.springkotlinbatch.domain

import com.springkotlinbatch.common.domain.BaseEntity
import jakarta.persistence.*

@Entity
class BatchDevice(
    deviceId: String,
    completed: Boolean = false,
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var deviceId: String = deviceId
        protected set

    var completed: Boolean = completed
        protected set

    fun toComplete(): BatchDevice {
        this.completed = true
        return this
    }
}
