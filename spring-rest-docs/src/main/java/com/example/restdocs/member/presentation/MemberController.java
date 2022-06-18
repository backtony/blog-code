package com.example.restdocs.member.presentation;


import com.example.restdocs.member.presentation.dto.MemberModificationRequest;
import com.example.restdocs.member.domain.MemberRepository;
import com.example.restdocs.member.presentation.dto.MemberResponse;
import com.example.restdocs.member.presentation.dto.MemberSignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Transactional
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/{id}")
    public MemberResponse getMember(@PathVariable Long id){
        return new MemberResponse(memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다.")));
    }

    @PostMapping
    public void createMember(@RequestBody @Valid MemberSignUpRequest dto){
        memberRepository.save(dto.toEntity());
    }

    @PatchMapping("/{id}")
    public void modify(@PathVariable Long id ,@RequestBody @Valid MemberModificationRequest dto){
        memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."))
                .modify(dto.getAge());
    }

    @GetMapping
    public Page<MemberResponse> getMembers(
            @PageableDefault(sort = "id",direction = Sort.Direction.DESC)Pageable pageable){
        return memberRepository.findAll(pageable).map(MemberResponse::new);
    }

}
