package com.example.restdocs.member.presentation.dto;


import com.example.restdocs.member.domain.Member;
import lombok.Getter;

@Getter
public class MemberResponse {
    private final Long id;
    private final String email;
    private final int age;

    public MemberResponse(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.age = member.getAge();
    }
}
