package com.example.jpalock.team.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    private String name;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        READY, CANCEL,PLAY
    }

    public Team(String name) {
        this.name = name;
        this.status = Status.READY;
    }

    public void play(){
        this.status = Status.PLAY;
    }

    public void cancel(){
        this.status = Status.CANCEL;
    }

}
