package com.springsecurity.authentication.infrastructure.repository;

import com.springsecurity.authentication.domain.LogoutRefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface LogoutRefreshTokenRedisRepository extends CrudRepository<LogoutRefreshToken,String> {
}
