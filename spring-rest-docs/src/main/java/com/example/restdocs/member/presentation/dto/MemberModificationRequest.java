package com.example.restdocs.member.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberModificationRequest {
    
    @Max(10)
    private int age;

    public static MemberModificationRequest of(int age){
        return MemberModificationRequest.builder()
                .age(age)
                .build();
    }


}
