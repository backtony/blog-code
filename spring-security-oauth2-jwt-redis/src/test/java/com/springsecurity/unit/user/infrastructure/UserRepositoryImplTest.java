package com.springsecurity.unit.user.infrastructure;

// todo 이거 sql 찍히는지 봐야 yml이 main것 적용되는지 test것 적용되는지 알듯

import com.springsecurity.support.JpaRepositoryTest;
import com.springsecurity.user.domain.User;
import com.springsecurity.user.domain.UserRepository;
import com.springsecurity.user.infrastructure.repository.UserJpaRepository;
import com.springsecurity.user.infrastructure.repository.UserRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static com.springsecurity.support.UserGivenHelper.givenUser;
import static org.assertj.core.api.Assertions.assertThat;

public class UserRepositoryImplTest extends JpaRepositoryTest {

    @Autowired UserJpaRepository userJpaRepository;

    UserRepository userRepository;

    @BeforeEach
    void setup(){
        userRepository = new UserRepositoryImpl(userJpaRepository);
    }

    @Test
    void User_저장() throws Exception{

        //when
        User savedUser = userRepository.save(givenUser());

        //then
        assertThat(userJpaRepository.existsById(savedUser.getId())).isTrue();
    }

    @Test
    void 이메일로_유저_찾기() throws Exception{
        //given
        User savedUser = userRepository.save(givenUser());

        //when
        Optional<User> userOptional = userRepository.findByEmail(savedUser.getEmail());
        
        //then
        assertThat(userOptional.get()).isNotNull();
    }

}
