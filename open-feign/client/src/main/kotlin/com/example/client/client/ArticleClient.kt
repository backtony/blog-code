package com.example.client.client

import com.example.client.client.dto.ArticleResponse
import com.example.client.client.dto.ArticleSaveCommand
import com.example.client.client.dto.ArticleUpdateCommand
import feign.*
import feign.codec.ErrorDecoder
import mu.KotlinLogging
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*


@FeignClient(name = "article", configuration = [ArticleFeignConfig::class])
interface ArticleClient {

    @PostMapping("/articles")
    fun save(@RequestBody articleSaveCommand: ArticleSaveCommand): ArticleResponse

    @GetMapping("/articles/{id}")
    fun get(@PathVariable id: String): ArticleResponse?

    @PatchMapping("/articles/{id}")
    fun update(@PathVariable id: String, @RequestBody articleUpdateCommand: ArticleUpdateCommand): ArticleResponse?

    @DeleteMapping("/articles/{id}")
    fun delete(@PathVariable id: String): ArticleResponse?

    @GetMapping("/error")
    fun error()

    @PostMapping(value = ["/upload"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestPart file: MultipartFile): String

    @GetMapping("/download/{path}")
    fun download(@PathVariable path: String): Response
}


@Configuration
class ArticleFeignConfig {

    private val log = KotlinLogging.logger { }

    @Bean
    fun authorizationHeaderInterceptor() = RequestInterceptor {
        it.header(HttpHeaders.AUTHORIZATION, "Bearer ${UUID.randomUUID()}")
    }

    // 빈은 생성하지 않으면 기본적으로 Retryer.NEVER_RETRY 타입의 Retryer 빈이 생성되어 재시도를 비활성화
    // 빈은 생성하면 IOException인 경우에 재시도가 발생 + errorDecorder에서 발생시킨 RetryableException에 대해 재시도
    @Bean
    fun retryer(): Retryer {
        // 1초를 시작으로 1.5를 곱하면서 재시도
        // 재시도 최대 간격은 2초
        // 최대 3번까지만 재시도
        return Retryer.Default(1000, 2000, 3)
    }

    @Bean
    fun errorDecoder(): ErrorDecoder {
        return ErrorDecoder { _, response ->
            when (response.status()) {
                401 -> RuntimeException("401 발생")
                500 ->
                    // retryer 빈등록으로 전체 에러에 대해 재시도하지 않고 여기서 retryableException으로 예외하면 재시도 가능
                    RetryableException(
                        response.status(),
                        "500 에러 발생, 재시도합니다.",
                        response.request().httpMethod(),
                        1,
                        response.request(),
                    )

                else -> RuntimeException("전대미문 article error 발생!")
            }
        }
    }
}
