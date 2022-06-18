package com.example.catalogservice.domain.service;

import com.example.catalogservice.domain.service.dto.response.CatalogInfoResponseDto;

import java.util.List;

public interface CatalogService {

    List<CatalogInfoResponseDto> getAllCatalogs();
}
