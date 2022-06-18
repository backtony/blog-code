package com.springsecurity.user.infrastructure.repository;

import com.springsecurity.user.domain.AuthProvider;
import com.springsecurity.user.domain.User;
import com.springsecurity.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmailAndAuthProvider(String email, AuthProvider authProvider) {
        return userJpaRepository.existsByEmailAndAuthProvider(email,authProvider);
    }

    @Override
    public Optional<User> findByEmailAndAuthProvider(String email, AuthProvider authProvider) {
        return userJpaRepository.findByEmailAndAndAuthProvider(email,authProvider);
    }
}
