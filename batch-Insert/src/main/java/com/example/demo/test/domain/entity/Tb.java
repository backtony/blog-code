package com.example.demo.test.domain.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Tb {

    @Id
    @TableGenerator(
            name = "TABLE_SEQ_GENERATOR",
            table = "TABLE_SEQUENCE",
            pkColumnName = "TABLE_SEQ"
    )
    @GeneratedValue(strategy = GenerationType.TABLE,
                    generator = "TABLE_SEQ_GENERATOR")
    private long id;

    private String title;
}
