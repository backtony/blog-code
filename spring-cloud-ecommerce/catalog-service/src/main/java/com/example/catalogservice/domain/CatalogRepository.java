package com.example.catalogservice.domain;

import java.util.List;
import java.util.Optional;

public interface CatalogRepository {

    List<Catalog> findAll();

    Optional<Catalog> findByProductId(String productId);
}
