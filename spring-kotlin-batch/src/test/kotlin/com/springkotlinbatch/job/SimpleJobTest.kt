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
// junit5는 기본적으로 @test를 실행할때마다 인스턴스를 개별로 하나씩 생성한다.
// 이것을 클래스 단위로만 하나로 생성해서 상태를 공유하도록 하려면 아래 애노테이션을 사용한다.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// @autowired를 일일이 붙이지 않고 그냥 생성자 안에 넣어서 주입받아서 사용하고 싶으면 이렇게 쓰면 된다.
@TestConstructor(autowireMode = AutowireMode.ALL)
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
