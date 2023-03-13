package com.springkotlinbatch.job.multithread

import com.springkotlinbatch.domain.BatchDevice
import com.springkotlinbatch.domain.Device
import com.springkotlinbatch.domain.DeviceRepository
import com.springkotlinbatch.job.simple.BatchDeviceProcessorFailureListener
import com.springkotlinbatch.job.simple.BatchDeviceReaderFailureListener
import com.springkotlinbatch.job.simple.BatchDeviceWriterFailureListener
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
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.PlatformTransactionManager
import java.lang.Boolean
import javax.sql.DataSource
import kotlin.Exception

@Configuration
class MultiThreadStepJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val entityManagerFactory: EntityManagerFactory,
    private val dataSource: DataSource,
    private val deviceRepository: DeviceRepository,
) {

    @Bean
    fun multiThreadJob(multiThreadStep: Step, jobRepository: JobRepository): Job {
        return JobBuilder(JobName, jobRepository)
            .start(multiThreadStep)
            .incrementer(RunIdIncrementer())
            .build()
    }

    @Bean
    @JobScope
    fun multiThreadStep(): Step {
        return StepBuilder(StepName, jobRepository)
            .chunk<BatchDevice, BatchDevice>(pageSize, transactionManager)
            .reader(multiBatchDeviceReader())
            .processor(multiBatchDeviceProcessor())
            .writer(multiBatchDeviceWriter())
            .taskExecutor(multiThreadStepJobTaskExecutor())
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
    fun multiThreadStepJobTaskExecutor(): TaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = poolSize
        executor.maxPoolSize = poolSize
        executor.setThreadNamePrefix("multi-thread-")
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE)
        executor.initialize()
        return executor
    }

    @Bean
    @StepScope
    fun multiBatchDeviceReader(): JpaPagingItemReader<BatchDevice> {
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
    fun multiBatchDeviceProcessor(): ItemProcessor<BatchDevice, BatchDevice> {
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
    fun multiBatchDeviceWriter(): JdbcBatchItemWriter<BatchDevice> {
        return JdbcBatchItemWriterBuilder<BatchDevice>()
            .dataSource(dataSource)
            .sql("UPDATE batch_device SET completed = true WHERE device_id = :deviceId")
            .beanMapped()
            .build()
    }

    companion object {
        private const val JobName = "MultiThreadStepJob"
        private const val StepName = "MultiThreadStepStep"
        private const val pageSize = 500

        private const val poolSize = 10
    }
}
