package com.example.jpalock.order.domain;

import javax.persistence.*;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        READY, SHIPPING, DELIVERED
    }

    public Order() {
        this.status = Status.READY;
    }


    public void startShipping() {
        this.status = Status.SHIPPING;
    }


    public boolean matchVersion(long version) {
        return this.version == version;
    }
}
