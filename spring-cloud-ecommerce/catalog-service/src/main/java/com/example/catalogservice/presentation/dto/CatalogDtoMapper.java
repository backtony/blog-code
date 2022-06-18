package com.example.catalogservice.presentation.dto;

import com.example.catalogservice.domain.service.dto.response.CatalogInfoResponseDto;
import com.example.catalogservice.presentation.dto.response.CatalogInfoResponse;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface CatalogDtoMapper {

    CatalogInfoResponse from (CatalogInfoResponseDto catalogInfoResponseDto);
}
