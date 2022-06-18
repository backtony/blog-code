package com.example.catalogservice.infrastructure;

import com.example.catalogservice.domain.Catalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CatalogJpaRepository extends JpaRepository<Catalog,Long> {

    Optional<Catalog> findByProductId(String productId);
}
