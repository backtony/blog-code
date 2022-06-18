package com.springsecurity.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;

@DataJpaTest
public abstract class JpaRepositoryTest {

    @Autowired
    EntityManager em;
}
