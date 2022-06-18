package com.example.demo.test.domain.repository;

import com.example.demo.test.domain.entity.Tb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TbRepository extends JpaRepository<Tb,Long> {
}
