package com.springkotlinbatch.service

import mu.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class JobExecuteService(
    private val asyncJobLauncher: JobLauncher,
    private val jobRegistry: JobRegistry,
    private val jobExplorer: JobExplorer,
) {
    private val logger = KotlinLogging.logger {}

    fun executeAsyncJob(jobName: String) {
        val now = LocalDateTime.now()

        try {
            val job = jobRegistry.getJob(jobName)
            val jobParameters = getRunIdAndLocalDateTimeJobParams(job, now)
            asyncJobLauncher.run(job, jobParameters)
        } catch (e: Exception) {
            logger.error { "job : $jobName, localDateTime : $now 실행에 실패했습니다. - ${e.message}" }
            e.printStackTrace()
        }
    }

    private fun getRunIdAndLocalDateTimeJobParams(job: Job, now: LocalDateTime): JobParameters {
        return JobParametersBuilder(jobExplorer)
            .getNextJobParameters(job)
            .addString("LocalDateTIme", now.toString())
            .toJobParameters()
    }

    fun isRunning(jobName: String): Boolean {
        return !jobExplorer.findRunningJobExecutions(jobName).isEmpty()
    }
}
