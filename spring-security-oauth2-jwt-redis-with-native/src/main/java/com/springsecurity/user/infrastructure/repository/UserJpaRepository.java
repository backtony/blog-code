package com.springsecurity.user.infrastructure.repository;

import com.springsecurity.user.domain.AuthProvider;
import com.springsecurity.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndAndAuthProvider(String email, AuthProvider authProvider);

    boolean existsByEmailAndAuthProvider(String email, AuthProvider authProvider);

}
