package com.example.catalogservice.domain.service;

import com.example.catalogservice.domain.CatalogRepository;
import com.example.catalogservice.domain.service.dto.response.CatalogInfoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService{

    private final CatalogRepository catalogRepository;

    @Override
    public List<CatalogInfoResponseDto> getAllCatalogs() {
        return catalogRepository.findAll().stream()
                .map(CatalogInfoResponseDto::from)
                .collect(Collectors.toList());
    }
}
