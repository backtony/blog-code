package com.example.restdocs.dummy;

import com.example.restdocs.member.domain.Member;
import com.example.restdocs.member.domain.MemberRepository;
import com.example.restdocs.member.domain.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSetup implements ApplicationRunner {

    private final MemberRepository memberRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        final List<Member> members = new ArrayList<>();
        members.add(new Member("abc1@naver.com",1, MemberStatus.BAN));
        members.add(new Member("abc2@naver.com",2,MemberStatus.NORMAL));
        members.add(new Member("abc3@naver.com",3,MemberStatus.BAN));
        members.add(new Member("abc4@naver.com",4,MemberStatus.NORMAL));
        memberRepository.saveAll(members);
    }
}
