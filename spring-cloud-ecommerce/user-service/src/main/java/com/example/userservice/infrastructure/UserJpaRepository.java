package com.example.userservice.infrastructure;

import com.example.userservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User,Long> {

    Optional<User> findByUserId(String userId);

    Optional<User> findByEmail(String email);

    @Query("select u.userId from User u where u.email =:email")
    String findUserIdByEmail(@Param("email") String email);
}
