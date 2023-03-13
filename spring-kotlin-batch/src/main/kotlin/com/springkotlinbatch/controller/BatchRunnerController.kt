package com.springkotlinbatch.controller

import com.springkotlinbatch.service.BatchDeviceService
import com.springkotlinbatch.service.JobExecuteService
import mu.KotlinLogging
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class BatchRunnerController(
    private val jobExecuteService: JobExecuteService,
    private val batchDeviceService: BatchDeviceService,
) {

    private val logger = KotlinLogging.logger {}

    @PostMapping("/v1/run/{jobName}")
    fun uploadBatchDevice(@RequestParam file: MultipartFile, @PathVariable("jobName") jobName: String) {
        batchDeviceService.uploadBatchDevice(file.inputStream)

        if (jobExecuteService.isRunning(jobName)) {
            logger.warn("BatchDevice was updated but $jobName is already running.")
        } else {
            logger.info { "run job : $jobName" }
            jobExecuteService.executeAsyncJob(jobName)
        }
    }
}
