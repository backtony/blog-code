package com.springsecurity.support.restdocs;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SampleRequest {

    @Email(message = "올바르지 않은 이메일입니다.")
    private String email;

}