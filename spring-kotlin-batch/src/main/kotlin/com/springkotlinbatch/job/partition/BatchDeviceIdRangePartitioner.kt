package com.springkotlinbatch.job.partition

import com.springkotlinbatch.domain.BatchDeviceRepository
import mu.KotlinLogging
import org.springframework.batch.core.partition.support.Partitioner
import org.springframework.batch.item.ExecutionContext
import org.springframework.stereotype.Component

@Component
class BatchDeviceIdRangePartitioner(
    private val batchDeviceRepository: BatchDeviceRepository,
) : Partitioner {

    private val logger = KotlinLogging.logger {}

    override fun partition(gridSize: Int): MutableMap<String, ExecutionContext> {
        val min: Long = batchDeviceRepository.findMinIdAndCompleted() ?: 0
        val max: Long = batchDeviceRepository.findMaxIdAndCompleted() ?: 0
        val targetSize = (max - min) / gridSize + 1

        logger.info { "==============" }
        logger.info { "$min $max" }
        logger.info { "==============" }

        val result: MutableMap<String, ExecutionContext> = HashMap()
        var number: Long = 0
        var start = min
        var end = start + targetSize - 1

        while (start <= max) {
            val value = ExecutionContext()
            result["partition$number"] = value

            logger.info { "==============" }
            logger.info { "partition$number, $start $end" }
            logger.info { "==============" }
            if (end >= max) {
                end = max
            }
            value.putLong("minId", start) // 각 파티션마다 사용될 minId
            value.putLong("maxId", end) // 각 파티션마다 사용될 maxId
            start += targetSize
            end += targetSize
            number++
        }

        return result
    }
}
