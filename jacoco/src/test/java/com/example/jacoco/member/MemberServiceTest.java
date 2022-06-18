package com.example.jacoco.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock MemberRepository memberRepository;
    @InjectMocks MemberService memberService;

    @Test
    @DisplayName("save")
    void save() throws Exception{
        //given
        Member member = Member.of("backtony", 27);

        //when
        memberService.save(member);

        //then
        verify(memberRepository,times(1)).save(any());
    }

}