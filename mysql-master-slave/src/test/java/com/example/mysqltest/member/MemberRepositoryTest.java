package com.example.mysqltest.member;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 데이터 소스 자동 연결 해
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("Save시 데이터 소스는 Master를 선택한다.")
    void save_Member_Success() throws Exception{
        //given
        Member member = Member.of("backtony", 26);
        memberRepository.save(member);
    }

    @Test
    @DisplayName("Slave DB에서 데이터를 조회한다 - 여러번 조회시 slave db 를 번갈아가면서 조회한다.")
    void findMember_Success() throws Exception{
        //given
        int age = 27;
        String name = "backtony";
        Member save = memberRepository.save(Member.of(name, age));

        //when
        Member member = memberRepository.findById(save.getId()).get();
        Member member1 = memberRepository.findById(save.getId()).get();
        Member member2 = memberRepository.findById(save.getId()).get();
        Member member3 = memberRepository.findById(save.getId()).get();

        //then
        Assertions.assertThat(member.getAge()).isEqualTo(age);
        Assertions.assertThat(member.getName()).isEqualTo(name);
    }

}