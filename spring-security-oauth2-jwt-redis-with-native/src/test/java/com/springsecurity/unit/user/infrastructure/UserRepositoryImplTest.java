package com.springsecurity.unit.user.infrastructure;


import com.springsecurity.support.JpaRepositoryTest;
import com.springsecurity.user.domain.AuthProvider;
import com.springsecurity.user.domain.User;
import com.springsecurity.user.domain.UserRepository;
import com.springsecurity.user.infrastructure.repository.UserJpaRepository;
import com.springsecurity.user.infrastructure.repository.UserRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static com.springsecurity.support.UserGivenHelper.givenGoogleUser;
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
        User savedUser = userRepository.save(givenGoogleUser());

        //then
        assertThat(userJpaRepository.existsById(savedUser.getId())).isTrue();
    }

    @Test
    void 이메일로_유저_찾기() throws Exception{
        //given
        User savedUser = userRepository.save(givenGoogleUser());

        //when
        Optional<User> userOptional = userRepository.findByEmail(savedUser.getEmail());
        
        //then
        assertThat(userOptional.get()).isNotNull();
    }

    @Test
    void 이메일과_authProvider로_유저가_존재하는지_확인_True() throws Exception{
        //given
        User savedUser = userRepository.save(givenGoogleUser());

        //when
        boolean result = userRepository.existsByEmailAndAuthProvider(savedUser.getEmail(), savedUser.getAuthProvider());

        //then
        assertThat(result).isTrue();
    }

    @Test
    void 이메일과_authProvider로_유저가_존재하는지_확인_False() throws Exception{
        //when
        boolean result = userRepository.existsByEmailAndAuthProvider("backtony", AuthProvider.local);

        //then
        assertThat(result).isFalse();
    }

    @Test
    void 이메일과_authProvider로_유저_찾기_성공() throws Exception{
        //given
        User savedUser = userRepository.save(givenGoogleUser());

        //when
        User user = userRepository.findByEmailAndAuthProvider(savedUser.getEmail(), savedUser.getAuthProvider()).get();

        //then
        assertThat(user).isEqualTo(savedUser);
    }

    @Test
    void 이메일과_authProvider로_유저_찾기_실패() throws Exception{
        //given

        //when
        Optional<User> userOptional = userRepository.findByEmailAndAuthProvider("test", AuthProvider.local);

        //then
        assertThat(userOptional).isEqualTo(Optional.empty());
    }

}
