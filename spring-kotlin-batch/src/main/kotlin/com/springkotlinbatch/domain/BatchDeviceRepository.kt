package com.springkotlinbatch.domain

import com.querydsl.jpa.impl.JPAQueryFactory
import com.springkotlinbatch.domain.QBatchDevice.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

interface BatchDeviceRepositoryCustom {
    fun findMinIdAndCompleted(): Long?
    fun findMaxIdAndCompleted(): Long?
}

@Repository
interface BatchDeviceRepository : JpaRepository<BatchDevice, Long>, BatchDeviceRepositoryCustom

class BatchDeviceRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : QuerydslRepositorySupport(BatchDevice::class.java), BatchDeviceRepositoryCustom {

    override fun findMinIdAndCompleted(): Long? {
        return queryFactory
            .select(batchDevice.id)
            .from(batchDevice)
            .where(batchDevice.completed.isFalse)
            .orderBy(batchDevice.id.asc())
            .limit(1)
            .fetchOne()
    }

    override fun findMaxIdAndCompleted(): Long? {
        return queryFactory
            .select(batchDevice.id)
            .from(batchDevice)
            .where(batchDevice.completed.isFalse)
            .orderBy(batchDevice.id.desc())
            .limit(1)
            .fetchOne()
    }
}
