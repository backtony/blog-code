package com.springsecurity.authentication.infrastructure.repository;

import com.springsecurity.authentication.domain.LogoutAccessToken;
import org.springframework.data.repository.CrudRepository;

public interface LogoutAccessTokenRedisRepository extends CrudRepository<LogoutAccessToken,String> {
}
