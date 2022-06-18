package com.example.userservice.domain;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByUserId(String userId);

    Optional<User> findByEmail(String email);

    String findUserIdByEmail(String email);
}
