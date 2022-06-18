package com.example.jacoco.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public void save(Member member){
        if (member.getAuthority().equals(Authority.DIRECTOR)){
            System.out.println(Authority.DIRECTOR.name());
        }
        else if (member.getAuthority().equals(Authority.ADMIN)){
            System.out.println(Authority.ADMIN.name());
        }
        else{
            memberRepository.save(member);
        }
    }
}
