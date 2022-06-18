package com.example.demo.test.domain.repository;

import com.example.demo.test.domain.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board,Long> {
}
