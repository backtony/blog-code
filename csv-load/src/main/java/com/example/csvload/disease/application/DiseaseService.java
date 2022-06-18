package com.example.csvload.disease.application;

import com.example.csvload.disease.domain.Disease;
import com.example.csvload.disease.domain.DiseaseRepository;
import com.example.csvload.disease.dto.DiseaseCSVRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DiseaseService {

    private final DiseaseRepository diseaseRepository;

    public void insertDiseaseCSVFile(List<DiseaseCSVRequestDto> diseaseCSVRequestDtoList){
        List<Disease> diseaseList = diseaseCSVRequestDtoList.parallelStream()
                .map(DiseaseCSVRequestDto::toDisease)
                .collect(Collectors.toList());

        diseaseRepository.saveAll(diseaseList);

    }
}
