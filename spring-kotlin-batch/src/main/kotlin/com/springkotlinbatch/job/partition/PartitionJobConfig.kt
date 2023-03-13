package com.springkotlinbatch.job.partition

import com.springkotlinbatch.domain.BatchDevice
import com.springkotlinbatch.domain.BatchDeviceRepository
import com.springkotlinbatch.domain.Device
import com.springkotlinbatch.domain.DeviceRepository
import com.springkotlinbatch.job.simple.BatchDeviceProcessorFailureListener
import com.springkotlinbatch.job.simple.BatchDeviceReaderFailureListener
import com.springkotlinbatch.job.simple.BatchDeviceWriterFailureListener
import jakarta.persistence.EntityManagerFactory
import mu.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import java.lang.Boolean
import javax.sql.DataSource
import kotlin.Any
import kotlin.Exception
import kotlin.Int
import kotlin.String
import kotlin.to

/**
 * https://jojoldu.tistory.com/550
 */
@Configuration
class PartitionJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val entityManagerFactory: EntityManagerFactory,
    private val dataSource: DataSource,
    private val deviceRepository: DeviceRepository,
    private val batchDeviceRepository: BatchDeviceRepository,

) {

    private val logger = KotlinLogging.logger {}

    @Bean
    fun partitionJob(): Job {
        return JobBuilder(JobName, jobRepository)
            .start(partitionStepManager())
            .incrementer(RunIdIncrementer())
            .build()
    }

    // 마스터 Step이 어떤 Step을 Worker로 지정하여 파티셔닝을 할 것인지를 결정하고,
    // 이때 사용할 PartitionHandler 를 등록합니다.
    @Bean
    fun partitionStepManager(): Step {
        return StepBuilder("$StepName.manager", jobRepository)
            .partitioner(StepName, partitioner())
            .partitionHandler(partitionHandler())
            .build()
    }

    @Bean
    @StepScope
    fun partitioner(): BatchDeviceIdRangePartitioner {
        return BatchDeviceIdRangePartitioner(batchDeviceRepository)
    }

    // PartitionHandler는 매니저 (마스터) Step이 Worker Step를 어떻게 다룰지를 정의합니다.
    @Bean
    fun partitionHandler(): TaskExecutorPartitionHandler {
        val partitionHandler = TaskExecutorPartitionHandler()
        partitionHandler.step = partitionStep()
        partitionHandler.setTaskExecutor(partitionThreadJobTaskExecutor())
        partitionHandler.gridSize = poolSize
        return partitionHandler
    }

    @Bean
    fun partitionThreadJobTaskExecutor(): TaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = poolSize
        executor.maxPoolSize = poolSize
        executor.setThreadNamePrefix("partition-thread-")
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE)
        executor.initialize()
        return executor
    }

    // worker step 정의 : 일반 step
    @Bean
    fun partitionStep(): Step {
        return StepBuilder(StepName, jobRepository)
            .chunk<BatchDevice, BatchDevice>(pageSize, transactionManager)
            .reader(partitionBatchDeviceReader(null, null))
            .processor(partitionBatchDeviceProcessor())
            .writer(partitionBatchDeviceWriter())
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
    fun partitionBatchDeviceReader(
        @Value("#{stepExecutionContext[minId]}") minId: Long?,
        @Value("#{stepExecutionContext[maxId]}") maxId: Long?,
    ): JpaPagingItemReader<BatchDevice> {
        val batchDeviceReader = object : JpaPagingItemReader<BatchDevice>() {
            override fun getPage(): Int {
                return 0
            }
        }

        logger.info { "======" }
        logger.info { minId }
        logger.info { maxId }
        logger.info { "======" }
        val params: Map<String, Any?> = mutableMapOf("minId" to minId, "maxId" to maxId)

        batchDeviceReader.setQueryString("select b from BatchDevice b where b.id BETWEEN :minId AND :maxId")
        batchDeviceReader.setParameterValues(params)
        batchDeviceReader.pageSize = pageSize
        batchDeviceReader.setEntityManagerFactory(entityManagerFactory)
        batchDeviceReader.setName("batchDeviceReader")

        return batchDeviceReader
    }

    @Bean
    @StepScope
    fun partitionBatchDeviceProcessor(): ItemProcessor<BatchDevice, BatchDevice> {
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
    fun partitionBatchDeviceWriter(): JdbcBatchItemWriter<BatchDevice> {
        return JdbcBatchItemWriterBuilder<BatchDevice>()
            .dataSource(dataSource)
            .sql("UPDATE batch_device SET completed = true WHERE device_id = :deviceId")
            .beanMapped()
            .build()
    }

    companion object {
        private const val JobName = "PartitionJob"
        private const val StepName = "PartitionStep"
        private const val pageSize = 500

        private const val poolSize = 3
    }
}
