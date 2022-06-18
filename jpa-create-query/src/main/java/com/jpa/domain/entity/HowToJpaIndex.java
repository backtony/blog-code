package com.jpa.domain.entity;

import javax.persistence.*;

/**
 * jpa 에서 엔티티에 제약 조건 거는 방법을 알아보기 위한 클래스
 * ddl 시에만 생성되기에 실무에서는 굳이 남겨둘 필요는 없지만 개발자는 이를 보고 쿼리를 효율적으로 짤 수 있다.
 *
 * index는 말그대로 인덱스 주는 것
 * name : 인덱스 이름
 * columList : 인덱스 대상
 *
 * uniqueConstraints : DDL 생성 시에 유니크 제약조건을 만든다. 복합 유니크 제약조건도 가능하다. 스키마 자동 생성 기능을 사용해서 DDL을 만들 때만 사용된다.
 * name : 유니크 키 이름
 * columnNames : 적용 대상
 */

@Entity
@Table(indexes = {@Index(name = "idx_email",columnList = "email")}
        ,uniqueConstraints = {@UniqueConstraint(name = "email_name", columnNames = {"email","name"})})
public class HowToJpaIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String name;
}
