package com.example.elasticsearch.member.presentation.dto;

import lombok.Getter;

@Getter
public class MemberSaveRequest {

    private String name;

    private String nickname;

    private int age;

    private Long zoneId;

    private String description;

}
