package com.example.eventdriven.order.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "orders")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Getter
    public enum Status {
        ORDER,CANCEL,PENDING
    }

    public void cancel(){
        status = Status.CANCEL;
    }

    public void pending(){
        status = Status.PENDING;
    }
}
