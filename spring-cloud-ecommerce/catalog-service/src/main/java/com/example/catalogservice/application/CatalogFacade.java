package com.example.catalogservice.application;

import com.example.catalogservice.domain.service.CatalogService;
import com.example.catalogservice.domain.service.dto.response.CatalogInfoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogFacade {

    private final CatalogService catalogService;

    public List<CatalogInfoResponseDto> getAllCatalogs() {
        return catalogService.getAllCatalogs();
    }
}
