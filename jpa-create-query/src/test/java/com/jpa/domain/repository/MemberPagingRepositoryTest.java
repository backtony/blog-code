package com.jpa.domain.repository;

import com.jpa.domain.dto.MemberDto;
import com.jpa.domain.entity.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest

class MemberPagingRepositoryTest {
    @Autowired
    MemberPagingRepository memberPagingRepository;
    @Autowired
    EntityManager em;

    @BeforeEach
    void teardown() {
        // deleteAll의 경우 쿼리가 여러번 나간다.
        memberPagingRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("findByName page 페이징 쿼리")
    void findByName_paging_success() throws Exception{
        //given
        String name = "backtony";
        List<Member> memberList = createMemberList(name);
        memberPagingRepository.saveAll(memberList);
        em.flush();
        em.clear();

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,"age"));

        //when
        Page<Member> page = memberPagingRepository.findByName(name, pageRequest);

        //then
        List<Member> content = page.getContent();

        assertThat(content.size()).isEqualTo(10);
        assertThat(page.getTotalElements()).isEqualTo(100);
        assertThat(page.getNumber()).isEqualTo(0); // 페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(10); // 총 페이지 개수
        assertThat(page.isFirst()).isTrue(); // 총 페이지 개수
        assertThat(page.hasNext()).isTrue(); // 총 페이지 개수
    }

    @Test
    @DisplayName("findByName page 페이징 쿼리 - count 쿼리 최적화")
    void findFastByName_paging_success() throws Exception{
        //given
        String name = "backtony";
        List<Member> memberList = createMemberList(name);
        memberPagingRepository.saveAll(memberList);
        em.flush();
        em.clear();

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,"age"));

        //when
        Page<Member> page = memberPagingRepository.findFastByName(name, pageRequest);

        // repository 주석으로 설명했던 내용 -> 이렇게 변환해서 반환해야 한다.
        Page<MemberDto> toDto = page.map(MemberDto::new);

        //then
        List<Member> content = page.getContent();

        assertThat(content.size()).isEqualTo(10);
        assertThat(page.getTotalElements()).isEqualTo(100);
        assertThat(page.getNumber()).isEqualTo(0); // 페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(10); // 총 페이지 개수
        assertThat(page.isFirst()).isTrue(); // 총 페이지 개수
        assertThat(page.hasNext()).isTrue(); // 총 페이지 개수
    }

    @Test
    @DisplayName("findByName slice 페이징 쿼리")
    void findByName_slice_success() throws Exception{
        //given
        String name = "backtony";
        List<Member> memberList = createMemberList(name);
        memberPagingRepository.saveAll(memberList);
        em.flush();
        em.clear();

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,"age"));

        //when
        Slice<Member> slice = memberPagingRepository.findSliceByName(name, pageRequest);

        //then
        List<Member> content = slice.getContent();

        assertThat(content.size()).isEqualTo(10);
        assertThat(slice.getNumber()).isEqualTo(0); // 페이지 번호
        assertThat(slice.isFirst()).isTrue(); // 총 페이지 개수
        assertThat(slice.hasNext()).isTrue(); // 총 페이지 개수
    }

    @Test
    @DisplayName("findByAge 페이징 content만 가져오기")
    void findByAge_only_content_success() throws Exception{
        //given
        String name = "backtony";
        int age = 27;
        List<Member> memberList = createMemberList(name);
        memberPagingRepository.saveAll(memberList);
        em.flush();
        em.clear();

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,"age"));

        //when
        List<Member> result = memberPagingRepository.findByAge(age, pageRequest);

        //then

        assertThat(result.size()).isEqualTo(10);

    }





    private List<Member> createMemberList(String name) {
        List<Member> memberList = new ArrayList<>();
        for (int i=0;i<100; i++){
            memberList.add(Member.builder().name(name).age(27).build());
        }
        return memberList;
    }

}