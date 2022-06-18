package com.querydsl.domain.repository;

import com.querydsl.config.TestQuerydslConfig;
import com.querydsl.domain.entity.Member;
import com.querydsl.domain.entity.Zone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@Import({MemberQueryRepository.class, TestQuerydslConfig.class})
@DataJpaTest
class MemberQueryRepositoryTest {

    @Autowired MemberQueryRepository memberQueryRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired ZoneRepository zoneRepository;
    @Autowired EntityManager em;

    String name = "backtony";
    int age = 27;
    Member testMember = Member.builder().name(name).age(age).build();

    @BeforeEach
    void teardown(){
        zoneRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        // for dummy
        memberRepository.saveAll(createMemberList(name,age));
    }

    @Test
    @DisplayName("id로 회원 찾기")
    void findById_success() throws Exception{
        //given
        Member member = memberRepository.save(testMember);

        // when
        Member result = memberQueryRepository.findById(member.getId()).get();

        //then
        assertThat(result.getId()).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("이름과 나이로 회원 찾기")
    void findByNameAndAge() throws Exception{
        //given
        int age= 30;
        memberRepository.saveAll(createMemberList(name,age));

        //when
        List<Member> results = memberQueryRepository.findByNameAndAge(name, age);

        //then
        assertThat(results.size()).isEqualTo(100);
        assertThat(results.get(0).getAge()).isEqualTo(age);
        assertThat(results.get(0).getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("나이 사이 값으로 회원 찾기")
    void findByBetweenAge_success() throws Exception{
        //given
        int startAge = 29;
        int endAge = 30;
        memberRepository.saveAll(createMemberList(name,30));

        //when
        List<Member> results = memberQueryRepository.findByBetweenAge(startAge, endAge);

        //then
        assertThat(results.size()).isEqualTo(100);
        assertThat(results.get(0).getAge()).isEqualTo(endAge);
        assertThat(results.get(0).getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("시작하는 이름으로 찾기")
    void findByStartWithName_success() throws Exception{
        //given
        String name = "test";
        memberRepository.saveAll(createMemberList(name,30));

        //when
        List<Member> results = memberQueryRepository.findByStartWithName(name);

        //then
        assertThat(results.size()).isEqualTo(100);
        assertThat(results.get(0).getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("시작하는 이름으로 찾고 나이순 내림차순 정렬")
    void findByNameOrderByAgeDesc_success() throws Exception{
        //given
        int oldAge = 30;
        memberRepository.saveAll(createMemberList(name,oldAge));

        //when
        List<Member> results = memberQueryRepository.findByNameOrderByAgeDesc(name);

        //then
        assertThat(results.size()).isEqualTo(200);
        assertThat(results.get(0).getAge()).isEqualTo(oldAge);
        assertThat(results.get(199).getAge()).isEqualTo(age);
    }

    @Test
    @DisplayName("시작하는 이름으로 찾고 나이순 내림차순 정렬")
    void exist_success() throws Exception{
        //given

        Member saveMember = memberRepository.save(testMember);

        //when
        boolean exist = memberQueryRepository.exist(saveMember.getId());

        //then
        assertThat(exist).isTrue();
    }

    @Test
    @DisplayName("회원 id로 회원 찾으면서 Zone 패치 조인하기")
    void findWithZoneById_success() throws Exception{
        //given
        Zone zone = zoneRepository.save(Zone.builder().mainZone("zone").build());
        Member member = memberRepository.save(Member.builder().zone(zone).name(name).age(age).build());


        //when
        Member result = memberQueryRepository.findWithZoneById(member.getId()).get();

        //then
        assertThat(result.getAge()).isEqualTo(age);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getZone().getId()).isEqualTo(zone.getId());
        assertThat(result.getZone().getMainZone()).isEqualTo(zone.getMainZone());
    }

    @Test
    @DisplayName("회원 id 받아서 해당 회원 이름 변경하기")
    void updateName_success() throws Exception{
        //given
        String rename = "tony";
        Member member = memberRepository.save(testMember);

        //when
        memberQueryRepository.updateName(member.getId(),rename);
        // 벌크 연산은 반드시 flush clear
        em.flush();
        em.clear();

        //then
        Member result = memberQueryRepository.findById(member.getId()).get();

        assertThat(result.getName()).isEqualTo(rename);
    }




    private List<Member> createMemberList(String name, int age) {

        List<Member> memberList = new ArrayList<>();
        for (int i=0;i<100;i++){
            memberList.add(Member.builder().name(name).age(age).build());
        }
        return memberList;
    }


    private List<Member> createMemberList(String name, int age,Zone zone) {

        List<Member> memberList = new ArrayList<>();
        for (int i=0;i<100;i++){
            memberList.add(Member.builder().name(name).age(age).zone(zone).build());
        }
        return memberList;
    }


}