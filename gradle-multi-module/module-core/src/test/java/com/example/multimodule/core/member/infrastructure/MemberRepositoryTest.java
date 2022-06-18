package com.example.multimodule.core.member.infrastructure;

import com.example.multimodule.core.member.domain.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(MemberRepositoryImpl.class)
public class MemberRepositoryTest {

    @Autowired MemberRepositoryImpl memberRepository;

    @Test
    void 회원_저장() {
        //given
        Member member = Member.builder().name("회원").build();

        //when
        Member result = memberRepository.save(member);

        //then
        assertThat(result.getName()).isEqualTo(member.getName());
    }

}
