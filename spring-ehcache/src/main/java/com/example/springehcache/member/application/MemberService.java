package com.example.springehcache.member.application;

import com.example.springehcache.member.application.dto.MemberInfoResponseDto;
import com.example.springehcache.member.domain.Member;
import com.example.springehcache.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public void saveMember(){
        memberRepository.save(Member.builder()
                .name("member1")
                .age(27)
                .build());
    }


    @Cacheable(
            value = "memberInfoResponseDto",
            key = "#memberId",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public MemberInfoResponseDto getMember(Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("member not found"));
        return MemberInfoResponseDto.from(member);
    }


    @CacheEvict(
            value = "memberInfoResponseDto",
            key = "#memberId"
    )
    public void updateMemberName(Long memberId, String name){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("member not found"));

        member.changeName(name);
    }

}
