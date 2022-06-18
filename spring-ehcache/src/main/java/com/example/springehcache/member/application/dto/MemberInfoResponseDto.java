package com.example.springehcache.member.application.dto;

import com.example.springehcache.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class MemberInfoResponseDto implements Serializable {

    private String name;
    private int age;

    public static MemberInfoResponseDto from(Member member){
        return new MemberInfoResponseDto(member.getName(),member.getAge());
    }
}
