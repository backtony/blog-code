package com.example.client.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class ClientAppConfig {
    // config
    // https://godekdls.github.io/Reactive%20Spring/webclient/
    // https://github.com/backtony/blog-code/issues/58
    // https://medium.com/@odysseymoon/spring-webclient-%EC%82%AC%EC%9A%A9%EB%B2%95-5f92d295edc0
    // https://gngsn.tistory.com/198
    @Bean
    fun webClient(): WebClient {
        val httpClient: HttpClient = HttpClient
            .create(
                ConnectionProvider.builder("webclient-pool")
                    .maxConnections(100) // connection pool의 갯수
                    .pendingAcquireTimeout(Duration.ofMillis(5)) // 커넥션 풀에서 커넥션을 얻기 위해 기다리는 최대 시간
                    .pendingAcquireMaxCount(-1) // 커넥션 풀에서 커넥션을 가져오는 시도 횟수 (-1: no limit)
                    .maxIdleTime(Duration.ofMillis(2000L)) // 커넥션 풀에서 idle 상태의 커넥션을 유지하는 시간
                    .build()
            )
            // connection timeout 지정
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 500) // 응답 timeout 지정
            .responseTimeout(Duration.ofMillis(500)) // connection이 연결된 후 수행할 동작 정의
            .doOnConnected { connection ->
                connection // 특정 시간동안 읽을 수 있는 데이터가 없을 경우 예외 발생
                    .addHandlerLast(
                        ReadTimeoutHandler(
                            500,
                            TimeUnit.MILLISECONDS
                        )
                    ) // 특정 시간동안 쓰기 작업을 종료할 수 없을 경우 예외 발생
                    .addHandlerLast(
                        WriteTimeoutHandler(
                            500,
                            TimeUnit.MILLISECONDS
                        )
                    )
            }

        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }
}
