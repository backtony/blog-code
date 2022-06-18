package com.example.springehcache.member.application;

import com.example.springehcache.config.CacheKey;
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


    // value를 enum값으로 하려고 했는데 옵션에서 enum get 메서드 호출이 안된다.
    @Cacheable(
            value = "member",
            key = "#memberId",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public MemberInfoResponseDto getRedisMember(Long memberId){

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("member not found"));
        return MemberInfoResponseDto.from(member);
    }
}
