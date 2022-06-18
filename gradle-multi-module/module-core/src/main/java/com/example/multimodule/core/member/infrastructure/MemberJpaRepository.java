package com.example.multimodule.core.member.infrastructure;

import com.example.multimodule.core.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member,Long> {
}
