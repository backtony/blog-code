package com.example.restdocs.member.domain;

import com.example.restdocs.common.EnumType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MemberStatus implements EnumType {
    LOCK("일시 정지"),
    NORMAL("정상"),
    BAN("영구 정지");

    private final String description;

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getName() {
        return this.name();
    }
}