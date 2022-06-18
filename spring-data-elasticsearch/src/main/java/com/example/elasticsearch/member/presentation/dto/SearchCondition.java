package com.example.elasticsearch.member.presentation.dto;

import com.example.elasticsearch.member.domain.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchCondition {

    private Long id;

    private String name;

    private String nickname;

    private int age;

    private Status status;

    private Long zoneId;

}
