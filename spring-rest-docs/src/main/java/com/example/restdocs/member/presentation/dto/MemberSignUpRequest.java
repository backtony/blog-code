package com.example.restdocs.member.presentation.dto;

import com.example.restdocs.member.domain.MemberStatus;
import com.example.restdocs.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberSignUpRequest {

    @Email
    @Size(max = 40)
    private String email;

    @Max(10)
    private int age;

    private MemberStatus status;

    public Member toEntity(){
        return new Member(email,age,status);
    }

    public static MemberSignUpRequest of(String email, int age){
        return MemberSignUpRequest.builder()
                .email(email)
                .age(age)
                .build();
    }
}
