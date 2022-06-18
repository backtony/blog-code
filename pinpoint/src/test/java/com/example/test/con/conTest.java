package com.example.test.con;


import com.example.test.Member;
import com.example.test.Repository;
import net.bytebuddy.asm.MemberRemoval;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

//@SpringBootTest
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@Testcontainers
@ActiveProfiles("test")
class conTest {

    @Autowired
    Repository repository;

//    @Container
//    static MySQLContainer mySQLContainer = new MySQLContainer()
//            .withDatabaseName("sa")
//            ;


    @DisplayName("test")
    @Test
    void test() throws Exception{
        //given
        Member m1 = Member.builder()
                .name("test1")
                .build();

        //when
        repository.save(m1);

        //then
    }
}
