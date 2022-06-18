package com.example.mysqltest.member;


import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int age;

    public static Member of(String name, int age){
        return Member.builder()
                .name(name)
                .age(age)
                .build();
    }
}
