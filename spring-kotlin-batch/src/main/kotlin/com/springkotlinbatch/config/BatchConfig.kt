package com.springkotlinbatch.config

import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

/**
 * https://docs.spring.io/spring-batch/docs/current/reference/html/index-single.html#configuringJobLauncher
 */
@EnableAsync
@Configuration
class BatchConfig(
    private val jobRepository: JobRepository,
) {

    @Bean
    fun asyncJobLauncher(): JobLauncher {
        val jobLauncher = TaskExecutorJobLauncher()
        jobLauncher.setJobRepository(jobRepository)
        jobLauncher.setTaskExecutor(threadPoolTaskExecutor())
        jobLauncher.afterPropertiesSet()
        return jobLauncher
    }

    private fun threadPoolTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 25
        executor.maxPoolSize = 50
        executor.queueCapacity = 10
        executor.setThreadNamePrefix("async-job-task")
        executor.initialize()
        return executor
    }

    // Job 등록은 스프링 컨테이너가 시작할 때,
    // JobRegistryBeanPostProcessor에 의해서 자동으로 JobRegistry에 Job을 등록시켜줌.
    // 따라서 JobRegistryBeanPostProcessor를 스프링 빈으로 등록해야함.
    @Bean
    fun jobRegistryBeanPostProcessor(jobRegistry: JobRegistry): JobRegistryBeanPostProcessor {
        val beanPostProcessor = JobRegistryBeanPostProcessor()
        beanPostProcessor.setJobRegistry(jobRegistry)
        return beanPostProcessor
    }
}
