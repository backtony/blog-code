package com.example.csvload.disease.presentation;


import com.example.csvload.common.CSVUtil;
import com.example.csvload.disease.application.DiseaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/csv")
public class DiseaseRestController {

    private final DiseaseService diseaseService;

    @PostMapping
    public ResponseEntity<Void> insertCSVFile(@RequestPart MultipartFile file){
        diseaseService.insertDiseaseCSVFile(CSVUtil.toDiseaseCSVRequestDto(file));
        return ResponseEntity.ok().build();
    }
}
