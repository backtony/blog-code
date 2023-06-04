package com.example.client.adapter

import com.example.client.dto.MemberRequest
import com.example.server.dao.Member
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

// error handling
// https://medium.com/nerd-for-tech/webclient-error-handling-made-easy-4062dcf58c49
@Component
class MemberApi(
    private val webClient: WebClient
) {

    suspend fun save(memberRequest: MemberRequest): Member {
        return webClient.mutate()
            .baseUrl("http://localhost:8080/member")
            .build()
            .post()
            .bodyValue(memberRequest)
            .retrieve()
            .bodyToMono(Member::class.java)
            .onErrorResume {
                throw RuntimeException("member api save exception")
            }
            .awaitSingle()
    }

    suspend fun findById(memberId: Long): Member {
        return webClient.mutate()
            .baseUrl("http://localhost:8080/member")
            .build()
            .get()
            .uri { uriBuilder ->
                uriBuilder.path("/{id}")
                    .build(memberId)
            }
            .retrieve()
            .bodyToMono(Member::class.java)
            .onErrorResume {
                throw RuntimeException("member api findById exception")
            }
            .awaitSingle()
    }

    suspend fun findWithTeamById(memberId: Long): Member {
        return webClient.mutate()
            .baseUrl("http://localhost:8080/member/with-team")
            .build()
            .get()
            .uri { uriBuilder ->
                uriBuilder.path("/{id}")
                    .build(memberId)
            }
            .retrieve()
            .bodyToMono(Member::class.java)
            .onErrorResume {
                throw RuntimeException("member api findWithTeamById exception")
            }
            .awaitSingle()
    }

    suspend fun updateMember(memberId: Long, memberRequest: MemberRequest) {
        webClient.mutate()
            .baseUrl("http://localhost:8080/member")
            .build()
            .patch()
            .uri { uriBuilder ->
                uriBuilder.path("/{id}")
                    .build(memberId)
            }
            .bodyValue(memberRequest)
            .retrieve()
            .bodyToMono(Unit::class.java)
            .onErrorResume {
                throw RuntimeException("member api updateMember exception")
            }
            .awaitSingleOrNull()
    }

    suspend fun deleteById(memberId: Long) {
        webClient.mutate()
            .baseUrl("http://localhost:8080/member")
            .build()
            .delete()
            .uri { uriBuilder ->
                uriBuilder.path("/{id}")
                    .build(memberId)
            }
            .retrieve()
            .bodyToMono(Unit::class.java)
            .onErrorResume {
                throw RuntimeException("member api deleteById exception")
            }
            .awaitSingleOrNull()
    }

    suspend fun findAll(): Flow<Member> {
        return webClient.mutate()
            .baseUrl("http://localhost:8080/member")
            .build()
            .get()
            .retrieve()
            .bodyToFlux(Member::class.java)
            .onErrorResume {
                throw RuntimeException("member api findAll exception")
            }
            .asFlow()
    }

    suspend fun findAllWithTeam(): Flow<Member> {
        return webClient.mutate()
            .baseUrl("http://localhost:8080/member/with-team")
            .build()
            .get()
            .retrieve()
            .bodyToFlux(Member::class.java)
            .onErrorResume {
                throw RuntimeException("member api findAllWithTeam exception")
            }
            .asFlow()
    }
}
