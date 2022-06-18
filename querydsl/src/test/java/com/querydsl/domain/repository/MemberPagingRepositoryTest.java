package com.querydsl.domain.repository;

import com.querydsl.config.TestQuerydslConfig;
import com.querydsl.domain.dto.MemberDto;
import com.querydsl.domain.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import({MemberPagingRepository.class, TestQuerydslConfig.class})
@DataJpaTest
class MemberPagingRepositoryTest {

    @Autowired MemberPagingRepository memberPagingRepository;
    @Autowired MemberRepository memberRepository;

    @BeforeEach
    void teardown(){
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("회원 이름으로 회원 찾기 - 페이징")
    void findPagingByName_success() throws Exception{
        //given
        String name = "backtony";
        int age = 27;
        List<Member> memberList = createMemberList(name, age);
        memberRepository.saveAll(memberList);

        PageRequest pageRequest = PageRequest.of(0, 10);


        //when
        Page<MemberDto> result = memberPagingRepository.findPagingByName(name,pageRequest);

        //then
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.isLast()).isFalse();
        assertThat(result.isFirst()).isTrue();
        assertThat(result.getNumberOfElements()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(100);
        assertThat(result.getTotalPages()).isEqualTo(10);
    }

    @Test
    @DisplayName("회원 이름으로 회원 찾기 - 슬라이스")
    void findSliceByName_success() throws Exception{
        //given
        String name = "backtony";
        int age = 27;
        List<Member> memberList = createMemberList(name, age);
        memberRepository.saveAll(memberList);

        PageRequest pageRequest2 = PageRequest.of(1,10);

        //when
        Slice<MemberDto> result = memberPagingRepository.findSliceByName(name, pageRequest2);

        //then
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.getNumberOfElements()).isEqualTo(10);
        assertThat(result.isLast()).isFalse();
        assertThat(result.isFirst()).isFalse();
    }

    @Test
    @DisplayName("회원 이름으로 회원 찾기 - 슬라이스 NoOffset 쿼리 최적화 - lastId null 일때")
    void findSliceNoOffsetByName_success_lastId_null() throws Exception{
        //given
        String name = "backtony";
        int age = 27;
        List<Member> memberList = createMemberList(name, age);
        memberRepository.saveAll(memberList);

        PageRequest pageRequest = PageRequest.of(0,10);

        //when
        Slice<MemberDto> result = memberPagingRepository.findSliceNoOffsetByName(null,name, pageRequest);

        //then
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.getNumberOfElements()).isEqualTo(10);
        assertThat(result.isLast()).isFalse();
        assertThat(result.isFirst()).isTrue();
    }

    @Test
    @DisplayName("회원 이름으로 회원 찾기 - 슬라이스 NoOffset 쿼리 최적화 - lastId 존재")
    void findSliceNoOffsetByName_success_lastId_exist() throws Exception{
        //given
        String name = "backtony";
        int age = 27;
        List<Member> memberList = createMemberList(name, age);
        List<Member> members = memberRepository.saveAll(memberList);



        PageRequest pageRequest = PageRequest.of(1,10);

        //when
        Slice<MemberDto> result = memberPagingRepository.findSliceNoOffsetByName(members.get(50).getId(),name, pageRequest);

        //then
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.getNumberOfElements()).isEqualTo(10);
        assertThat(result.isLast()).isFalse();
        assertThat(result.isFirst()).isFalse();
    }

    @Test
    @DisplayName("회원 이름으로 회원 찾기 - 커버링 인덱스 최적화")
    void findCoveringIndexPagingByName_success() throws Exception{
        //given
        String name = "backtony";
        int age = 27;
        List<Member> memberList = createMemberList(name, age);
        memberRepository.saveAll(memberList);

        PageRequest pageRequest = PageRequest.of(0, 10);


        //when
        Page<MemberDto> result = memberPagingRepository.findCoveringIndexPagingByName(name,pageRequest);


        //then
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.getNumberOfElements()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(100);
        assertThat(result.getTotalPages()).isEqualTo(10);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isFalse();

    }

    @Test
    @DisplayName("회원 이름으로 회원 찾기 - 커버링 인덱스 최적화 - 빈값")
    void findCoveringIndexPagingByName_success_no_result() throws Exception{
        //given
        String name = "backtony";
        int age = 27;
        List<Member> memberList = createMemberList(name, age);
        memberRepository.saveAll(memberList);

        PageRequest pageRequest = PageRequest.of(10, 10);


        //when
        Page<MemberDto> result = memberPagingRepository.findCoveringIndexPagingByName(name,pageRequest);


        //then
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getNumber()).isEqualTo(10);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.getNumberOfElements()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(100);
        assertThat(result.getTotalPages()).isEqualTo(10);
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isTrue();

    }




    private List<Member> createMemberList(String name, int age) {
        List<Member> memberList = new ArrayList<>();
        for (int i=0;i<100;i++){
            memberList.add(Member.builder().name(name).age(age).build());
        }
        return memberList;
    }


}