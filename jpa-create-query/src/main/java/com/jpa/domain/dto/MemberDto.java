package com.jpa.domain.dto;

import com.jpa.domain.entity.Member;

public class MemberDto {

    private String name;
    private int age;

    public MemberDto(Member member) {
        this.name = member.getName();
        this.age = member.getAge();
    }
}
