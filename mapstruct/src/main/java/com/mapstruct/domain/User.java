package com.mapstruct.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    private String address;

    private String name;

    private int age;

    @Builder
    public User(String nickname, String address, String name, int age) {
        this.nickname = nickname;
        this.address = address;
        this.name = name;
        this.age = age;
    }
}
