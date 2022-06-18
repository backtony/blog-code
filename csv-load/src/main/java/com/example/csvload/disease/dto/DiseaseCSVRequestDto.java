package com.example.csvload.disease.dto;

import com.example.csvload.disease.domain.Disease;
import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseaseCSVRequestDto {

    @CsvBindByPosition(position = 0, required = true)
    private String name;

    @CsvBindByPosition(position = 1, required = true)
    private String definition;

    @CsvBindByPosition(position = 2, required = true)
    private String recommendDepartment;

    @CsvBindByPosition(position = 3, required = true)
    private String symptom;

    @CsvBindByPosition(position = 4, required = true)
    private String cause;

    @CsvBindByPosition(position = 5, required = true)
    private String hospitalCare;

    public Disease toDisease(){
        return Disease.builder()
                .name(name)
                .definition(definition)
                .recommendDepartment(recommendDepartment)
                .symptom(symptom)
                .cause(cause)
                .hospitalCare(hospitalCare)
                .build();
    }
}
