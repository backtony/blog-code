package com.querydsl.domain;

import com.querydsl.domain.dto.MemberDto;
import com.querydsl.domain.entity.Member;
import com.querydsl.domain.repository.MemberPagingRepository;
import com.querydsl.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class Controller {

    private final MemberRepository memberRepository;
    private final MemberPagingRepository memberPagingRepository;


    /**
     * slice가 어떻게 반환되는가 궁금해서 찍어본 Controller
     */

    @GetMapping("/nooffset")
    public ResponseEntity<Slice<MemberDto>> noOffset(){
        memberRepository.saveAll(createMemberList("backtony",27));
        PageRequest pageRequest = PageRequest.of(0, 10);
        Slice<MemberDto> result = memberPagingRepository.findSliceNoOffsetByName(50L, "backtony", pageRequest);
        return ResponseEntity.ok(result);
    }

    private List<Member> createMemberList(String name, int age) {
        List<Member> memberList = new ArrayList<>();
        for (int i=0;i<100;i++){
            memberList.add(Member.builder().name(name).age(age).build());
        }
        return memberList;
    }
}
