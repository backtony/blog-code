package com.jpa.domain.repository;

import com.jpa.domain.entity.MemberCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCategoryRepository extends JpaRepository<MemberCategory,Long> {
}
