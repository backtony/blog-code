package com.springkotlinbatch.service

import com.springkotlinbatch.util.CSVUtil
import com.springkotlinbatch.domain.BatchDevice
import com.springkotlinbatch.domain.BatchDeviceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.InputStream

@Service
@Transactional
class BatchDeviceService(
    private val batchDeviceRepository: BatchDeviceRepository,
) {

    fun uploadBatchDevice(inputStream: InputStream) {
        val batchDeviceList = CSVUtil.readCsv(inputStream)
        saveBatchDeviceList(batchDeviceList)
    }

    private fun saveBatchDeviceList(batchDeviceList: List<BatchDevice>) {
        for (chunkedBatchDeviceList in batchDeviceList.chunked(chunkSize)) {
            batchDeviceRepository.saveAll(chunkedBatchDeviceList)
        }
    }

    companion object {
        private const val chunkSize = 1000
    }
}
