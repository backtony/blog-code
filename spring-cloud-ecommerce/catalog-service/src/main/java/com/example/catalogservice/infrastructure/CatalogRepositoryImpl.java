package com.example.catalogservice.infrastructure;

import com.example.catalogservice.domain.Catalog;
import com.example.catalogservice.domain.CatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CatalogRepositoryImpl implements CatalogRepository {

    private final CatalogJpaRepository catalogJpaRepository;

    @Override
    public List<Catalog> findAll() {
        return catalogJpaRepository.findAll();
    }

    @Override
    public Optional<Catalog> findByProductId(String productId) {
        return catalogJpaRepository.findByProductId(productId);
    }
}
