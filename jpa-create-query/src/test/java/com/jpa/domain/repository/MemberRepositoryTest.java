package com.jpa.domain.repository;

import com.jpa.domain.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired ZoneRepository zoneRepository;
    @Autowired TeamRepository teamRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired MemberCategoryRepository memberCategoryRepository;
    @Autowired EntityManager em;

    @BeforeEach
    void teardown(){
        // deleteAll의 경우 쿼리가 여러번 나간다.
        memberRepository.deleteAllInBatch();
        zoneRepository.deleteAllInBatch();
        teamRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        memberCategoryRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("나이로 회원 찾기")
    void findByAge_success() throws Exception{
        //given
        int age = 26;

        Member member = Member.builder().age(age).build();
        memberRepository.save(member);

        //when
        Member result = memberRepository.findByAge(age).get(0);

        //then
        assertThat(result.getAge()).isEqualTo(age);
    }

    @Test
    @DisplayName("yml에 설정한 default batch 옵션 적용되는지 확인")
    void findByAge_and_get_memberCategory_check_batch_option_work() throws Exception{
        //given
        String name ="backtony";
        int age = 26;
        Member member = Member.builder().name(name).age(age).build();
        Member member2 = Member.builder().name(name).age(age).build();
        Member saveMember = memberRepository.save(member);
        memberRepository.save(member2);

        Category category1 = Category.builder().mainCategory(name).build();
        Category category2 = Category.builder().mainCategory(name).build();
        categoryRepository.saveAll(List.of(category1,category2));

        MemberCategory mc1 = MemberCategory.builder().category(category1).member(saveMember).build();
        MemberCategory mc2 = MemberCategory.builder().category(category2).member(saveMember).build();
        memberCategoryRepository.saveAll(List.of(mc1,mc2));
        em.flush();
        em.clear();

        //when
        List<Member> memberList = memberRepository.findByAge(age);

        //then
        System.out.println("==============");
        memberList.get(0).getMemberCategories().get(0);
        System.out.println("==============");
    }

    @Test
    @DisplayName("이름으로 회원 찾으면서 team, zone fetch로 가져오기")
    void findByName_success() throws Exception{
        //given
        String name ="backtony";
        Zone zone = Zone.builder().mainZone(name).build();
        Team team = Team.builder().name(name).build();
        Member member = Member.builder().name(name).zone(zone).team(team).build();

        zoneRepository.save(zone);
        teamRepository.save(team);
        memberRepository.save(member);

        //when
        Member result = memberRepository.findByName(name).get();

        //then
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getTeam()).isEqualTo(team);
        assertThat(result.getZone()).isEqualTo(zone);
    }

    @Test
    @DisplayName("name, age로 회원 찾기")
    void findByNameAndAge_success() throws Exception{
        //given
        String name = "backtony";
        int age = 26;

        Member member = Member.builder().name(name).age(age).build();
        memberRepository.save(member);

        //when
        Member result = memberRepository.findByNameAndAge(name, age).get();

        //then
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getAge()).isEqualTo(age);
    }

    @Test
    @DisplayName("이름으로 회원 찾으면서 team fetch로 가져오기")
    void findWithTeamByName_success() throws Exception{
        //given
        String name ="backtony";
        Team team = Team.builder().name(name).build();
        Member member = Member.builder().name(name).team(team).build();

        teamRepository.save(team);
        memberRepository.save(member);


        //when
        Member result = memberRepository.findWithTeamByName(name).get();

        //then
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getTeam()).isEqualTo(team);
    }

    @Test
    @DisplayName("이름으로 회원 찾으면서 memberCategories fetch 하기")
    void findWithMemberCategoryByName_success() throws Exception{
        //given
        String name ="backtony";
        Member member = Member.builder().name(name).build();
        Member saveMember = memberRepository.save(member);

        Category category1 = Category.builder().mainCategory(name).build();
        Category category2 = Category.builder().mainCategory(name).build();
        categoryRepository.saveAll(List.of(category1,category2));

        MemberCategory mc1 = MemberCategory.builder().category(category1).member(saveMember).build();
        MemberCategory mc2 = MemberCategory.builder().category(category2).member(saveMember).build();
        memberCategoryRepository.saveAll(List.of(mc1,mc2));
        em.flush();
        em.clear();

        //when
        Member result = memberRepository.findWithMemberCategoryByName(name).get();

        //then
        assertThat(result.getMemberCategories().get(0)).isEqualTo(mc1);
        assertThat(result.getMemberCategories().get(1)).isEqualTo(mc2);
    }



    @Test
    @DisplayName("회원 이름 수정")
    void updateNameByAge_success() throws Exception{
        //given
        String name = "backtony";
        int age = 26;
        Member member = Member.builder().name(name).age(age).build();
        memberRepository.save(member);

        //when
        memberRepository.updateNameByAge(name,age+1);

        //then
        Member result = memberRepository.findByName(name).get();
        assertThat(result.getAge()).isEqualTo(age+1);
    }


}