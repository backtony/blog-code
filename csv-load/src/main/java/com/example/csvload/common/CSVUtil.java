package com.example.csvload.common;

import com.example.csvload.disease.dto.DiseaseCSVRequestDto;
import com.example.csvload.exception.disease.FileReadException;
import com.example.csvload.exception.disease.NotCSVFileException;
import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

public class CSVUtil {

    private static final String TYPE = "text/csv";

    public static List<DiseaseCSVRequestDto> toDiseaseCSVRequestDto(MultipartFile file) {
        CheckIsCSVFile(file);
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            return new CsvToBeanBuilder<DiseaseCSVRequestDto>(reader)
                    .withType(DiseaseCSVRequestDto.class)
                    .build()
                    .parse();
        } catch (RuntimeException | IOException e) {
            throw new FileReadException();
        }
    }

    private static void CheckIsCSVFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileReadException("해당 파일이 존재하지 않습니다.");
        }
        if (!TYPE.equals(file.getContentType())) {
            throw new NotCSVFileException();
        }
    }

}
