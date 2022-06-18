package com.example.catalogservice.presentation;

import com.example.catalogservice.application.CatalogFacade;
import com.example.catalogservice.presentation.dto.CatalogDtoMapper;
import com.example.catalogservice.presentation.dto.response.CatalogInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CatalogRestController {

    private final CatalogFacade catalogFacade;
    private final CatalogDtoMapper catalogDtoMapper;


    @GetMapping("/catalogs")
    public ResponseEntity<List<CatalogInfoResponse>> getCatalogs() {
        List<CatalogInfoResponse> catalogInfoResponseList = catalogFacade.getAllCatalogs().stream()
                .map(catalogDtoMapper::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(catalogInfoResponseList);
    }
}
