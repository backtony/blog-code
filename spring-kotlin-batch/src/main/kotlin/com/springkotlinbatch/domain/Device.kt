package com.springkotlinbatch.domain

import com.springkotlinbatch.common.domain.AbstractAggregationRootEntity
import com.springkotlinbatch.common.exeception.ResourceNotFoundException
import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@EntityListeners(AuditingEntityListener::class)
@Entity
class Device(
    deviceId: String,
    company: Company = Company.APPLE,
) : AbstractAggregationRootEntity<Device>() {

    @Id
    var deviceId: String = deviceId
        protected set

    @Enumerated(EnumType.STRING)
    var company: Company = company

    companion object {
        fun deviceNotFound(deviceId: String): ResourceNotFoundException {
            return ResourceNotFoundException.notFound("device", deviceId)
        }
    }
}

enum class Company {
    SAMSUNG, LG, KT, APPLE
}
