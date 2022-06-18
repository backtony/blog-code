package com.springsecurity.support;

import com.springsecurity.config.EmbeddedRedisConfig;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Import;

@Import({EmbeddedRedisConfig.class})
@DataRedisTest
public class RedisRepositoryTest {
}
