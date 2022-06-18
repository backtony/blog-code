package com.springsecurity.user.domain;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(String email);

    boolean existsByEmailAndAuthProvider(String email, AuthProvider authProvider);

    Optional<User> findByEmailAndAuthProvider(String email, AuthProvider authProvider);
}
