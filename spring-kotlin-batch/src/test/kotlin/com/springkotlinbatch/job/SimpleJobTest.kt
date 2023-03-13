package com.springkotlinbatch.job

import com.springkotlinbatch.domain.BatchDevice
import com.springkotlinbatch.domain.BatchDeviceRepository
import com.springkotlinbatch.domain.DeviceRepository
import com.springkotlinbatch.support.BatchTestSupport
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.Job
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode
import java.util.*

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // @test 메서드마다 test 클래스가 새로 생성되는 것을 방지 - 계속 재사용
@TestConstructor(autowireMode = AutowireMode.ALL) // test에 인자 주입받을 때 사용
class SimpleJobTest(
    private val simpleJob: Job,
    private val batchDeviceRepository: BatchDeviceRepository,
    private val deviceRepository: DeviceRepository,
) : BatchTestSupport() {

    @BeforeAll
    fun init() {
        initBatchTestSupport(simpleJob)
    }

    @BeforeEach
    fun teardown() {
        deviceRepository.deleteAllInBatch()
        batchDeviceRepository.deleteAllInBatch()
    }

    @Test
    fun testSimpleJob_should_change_the_status_of_batchDevice_to_completed() {
        // given
        val batchDevices = mutableListOf<BatchDevice>()
        val count = 150L
        for (idx in 1..count) {
            batchDevices.add(BatchDevice(UUID.randomUUID().toString()))
        }
        batchDeviceRepository.saveAll(batchDevices)

        // when
        val jobExecution = jobLauncherTestUtils.launchJob()

        // then
        assertThat(batchDeviceRepository.findAll().filter { it.completed }.size).isEqualTo(count)
        assertThat(deviceRepository.count()).isEqualTo(count)

        assertThat(jobExecution).isNotNull
        assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
    }
}
