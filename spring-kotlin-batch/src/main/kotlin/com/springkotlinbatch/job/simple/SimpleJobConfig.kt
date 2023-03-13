package com.springkotlinbatch.job.simple

import com.springkotlinbatch.domain.BatchDevice
import com.springkotlinbatch.domain.Device
import com.springkotlinbatch.domain.DeviceRepository
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

/**
 * https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-5.0-Migration-Guide
 * https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide#spring-batch-changes
 */

@Configuration
class SimpleJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val entityManagerFactory: EntityManagerFactory,
    private val dataSource: DataSource,
    private val deviceRepository: DeviceRepository,
) {

    @Bean
    fun simpleJob(): Job {
        return JobBuilder(JobName, jobRepository)
            .start(simpleStep())
            .incrementer(RunIdIncrementer())
            .build()
    }

    @Bean
    @JobScope
    fun simpleStep(): Step {
        return StepBuilder(StepName, jobRepository)
            .chunk<BatchDevice, BatchDevice>(pageSize, transactionManager)
            .reader(simpleBatchDeviceReader())
            .processor(simpleBatchDeviceProcessor())
            .writer(simpleBatchDeviceWriter())
            .listener(BatchDeviceReaderFailureListener())
            .listener(BatchDeviceProcessorFailureListener())
            .listener(BatchDeviceWriterFailureListener())
            .faultTolerant()
            .retry(Exception::class.java)
            .retryLimit(3)
            .skip(Exception::class.java)
            .skipLimit(50)
            .build()
    }

    @Bean
    @StepScope
    fun simpleBatchDeviceReader(): JpaPagingItemReader<BatchDevice> {
        val batchDeviceReader = object : JpaPagingItemReader<BatchDevice>() {
            override fun getPage(): Int {
                return 0
            }
        }
        val params = mapOf("completed" to false)

        batchDeviceReader.setQueryString("select b from BatchDevice b where b.completed = :completed")
        batchDeviceReader.pageSize = pageSize
        batchDeviceReader.setEntityManagerFactory(entityManagerFactory)
        batchDeviceReader.setName("batchDeviceReader")
        batchDeviceReader.setParameterValues(params)

        return batchDeviceReader
    }

    @Bean
    @StepScope
    fun simpleBatchDeviceProcessor(): ItemProcessor<BatchDevice, BatchDevice> {
        return ItemProcessor<BatchDevice, BatchDevice> { batchDevice: BatchDevice ->
            if (!deviceRepository.existsById(batchDevice.deviceId)) {
                deviceRepository.save(
                    Device(batchDevice.deviceId),
                )
            }
            batchDevice
        }
    }

    @Bean
    @StepScope
    fun simpleBatchDeviceWriter(): JdbcBatchItemWriter<BatchDevice> {
        return JdbcBatchItemWriterBuilder<BatchDevice>()
            .dataSource(dataSource)
            .sql("UPDATE batch_device SET completed = true WHERE device_id = :deviceId")
            .beanMapped()
            .build()
    }

    companion object {
        private const val JobName = "SimpleJob"
        private const val StepName = "SimpleStep"
        private const val pageSize = 500
    }
}
