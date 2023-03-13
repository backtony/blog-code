package com.springkotlinbatch.job.simple

import com.springkotlinbatch.domain.BatchDevice
import mu.KotlinLogging
import org.springframework.batch.core.ItemProcessListener
import org.springframework.batch.core.ItemReadListener
import org.springframework.batch.core.ItemWriteListener
import org.springframework.batch.item.Chunk

class BatchDeviceReaderFailureListener : ItemReadListener<BatchDevice> {

    private val logger = KotlinLogging.logger {}

    override fun onReadError(ex: Exception) {
        logger.error("Encountered error on read", ex)
    }
}

class BatchDeviceProcessorFailureListener : ItemProcessListener<BatchDevice, BatchDevice> {
    private val logger = KotlinLogging.logger {}

    override fun onProcessError(item: BatchDevice, e: Exception) {
        logger.error("Encountered error on process deviceId : ${item.deviceId}", e)
    }
}

class BatchDeviceWriterFailureListener : ItemWriteListener<BatchDevice> {

    private val logger = KotlinLogging.logger {}

    override fun onWriteError(exception: Exception, items: Chunk<out BatchDevice>) {
        logger.error("Encountered error on write", exception)
    }
}
