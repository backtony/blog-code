package com.example.multimodule.batch.member.application;

import com.example.multimodule.core.member.application.MemberService;
import com.example.multimodule.core.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 지금은 간단해서 하나씩만 있지만 복잡해지게 되면
 * 여러 domain service 모듈을 조합하여 로직 작성
 */

@Service
@RequiredArgsConstructor
public class BatchFacade {

    private final MemberService memberService;

    public void saveAnyMember(){
        memberService.saveAnyMember();
    }

    public Member findAnyMember(){
        return memberService.findAnyMember();
    }
}
