package com.example.springehcache.member.presentation.dto;


import com.example.springehcache.member.application.dto.MemberInfoResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberInfoResponse {
    private String name;
    private int age;

    public static MemberInfoResponse from(MemberInfoResponseDto memberInfoResponseDto){
        return new MemberInfoResponse(memberInfoResponseDto.getName(),memberInfoResponseDto.getAge());
    }
}
