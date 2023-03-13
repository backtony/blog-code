package com.springkotlinbatch.support

import org.springframework.batch.core.Job
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.task.SyncTaskExecutor

open class BatchTestSupport {

    @Autowired
    private lateinit var jobRepository: JobRepository
    protected val jobLauncherTestUtils = JobLauncherTestUtils()

    protected fun initBatchTestSupport(job: Job) {
        val jobLauncher = createJobLauncher()
        jobLauncherTestUtils.job = job
        jobLauncherTestUtils.jobLauncher = jobLauncher
        jobLauncherTestUtils.jobRepository = jobRepository
    }

    private fun createJobLauncher(): JobLauncher {
        val simpleJobLauncher = TaskExecutorJobLauncher()
        simpleJobLauncher.setJobRepository(jobRepository)
        // test code 에서는 동기 방식으로 job 실행 (job 성공 결과 확인을 위함)
        simpleJobLauncher.setTaskExecutor(SyncTaskExecutor())
        simpleJobLauncher.afterPropertiesSet()
        return simpleJobLauncher
    }
}
