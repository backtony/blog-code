package com.springkotlinbatch.util

import com.springkotlinbatch.common.exeception.FileReadException
import com.springkotlinbatch.domain.BatchDevice
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.springframework.util.StringUtils
import java.io.InputStream

object CSVUtil {

    fun readCsv(inputStream: InputStream): List<BatchDevice> {
        return getCSVFormat().parse(inputStream.reader())
            .asSequence()
            .mapIndexed { line, csvRecord ->
                BatchDeviceValidator.validate(csvRecord, line + 1)
                csvRecord[0]
            }
            .distinct()
            .map { BatchDevice(it) }
            .toList()
    }

    private fun getCSVFormat(): CSVFormat {
        return CSVFormat.Builder.create(CSVFormat.DEFAULT).apply {
            setIgnoreSurroundingSpaces(true)
            setHeader()
            setSkipHeaderRecord(true)
            setIgnoreEmptyLines(true)
        }.build()
    }
}

object BatchDeviceValidator {

    fun validate(csvRecord: CSVRecord, line: Int) {
        checkColumnSize(csvRecord, line)
        checkWhiteSpace(csvRecord, line)
        checkIsBlank(csvRecord, line)
        checkDeviceId(csvRecord, line)
    }

    private fun checkColumnSize(csvRecord: CSVRecord, line: Int) {
        if (csvRecord.size() != 1) {
            throw FileReadException("The csv file must have only one column. line : $line")
        }
    }

    private fun checkWhiteSpace(csvRecord: CSVRecord, line: Int) {
        if (StringUtils.containsWhitespace(csvRecord[0])) {
            throw FileReadException("DeviceId cannot contain spaces. line : $line, record : ${csvRecord[0]}")
        }
    }

    private fun checkIsBlank(csvRecord: CSVRecord, line: Int) {
        if (csvRecord[0].isBlank()) {
            throw FileReadException("DeviceId cannot be blank. line : $line")
        }
    }

    private fun checkDeviceId(csvRecord: CSVRecord, line: Int) {
        if (csvRecord[0].length > 50) {
            throw FileReadException("DeviceId is too long. line : $line, record : ${csvRecord[0]}")
        }
    }
}
