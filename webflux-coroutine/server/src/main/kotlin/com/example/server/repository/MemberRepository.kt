package com.example.server.repository

import com.example.server.dao.Member
import com.example.server.dao.Team
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.LocalDateTime

interface MemberRepositoryCustom {
    suspend fun findWithTeamById(memberId: Long): Member?

    // flow(비동기적인 데이터 스트림)를 반환하는 함수는 suspend 키워드를 붙이지 않는다.
    // cold stream으로 flow가 collect되는 시점에 실행된다.
    // collect 하는 곳에서는 suspend 키워드가 붙어있어야 한다.(코루틴 스코프나 suspend 함수 내에서 collect되어야 한다.)
    fun findAllWithTeam(): Flow<Member>
}

interface MemberRepository : CoroutineCrudRepository<Member, Long>, MemberRepositoryCustom

@Repository
class MemberRepositoryCustomImpl(
    private val client: DatabaseClient
) : MemberRepositoryCustom {

    override suspend fun findWithTeamById(memberId: Long): Member? {
        val query = """
                        SELECT m.*,
                        t.name AS team_name,
                        t.leader_id AS team_leader_id,
                        t.created_at AS team_created_at,
                        t.modified_at AS team_modified_at
                        FROM member m
                        LEFT JOIN Team t
                        ON m.team_id = t.team_id
                        WHERE member_id = :memberId
        """.trimIndent()

        return client.sql(query)
            .bind("memberId", memberId)
            .fetch()
            .one()
            .map { memberWithTeamMapper(it) }
            .awaitSingleOrNull()
    }

    override fun findAllWithTeam(): Flow<Member> {
        val query = """
                        SELECT m.*,
                        t.name AS team_name,
                        t.leader_id AS team_leader_id,
                        t.created_at AS team_created_at,
                        t.modified_at AS team_modified_at
                        FROM member m
                        LEFT JOIN Team t
                        ON m.team_id = t.team_id
        """.trimIndent()

        return client.sql(query)
            .fetch()
            .all()
            .flatMap { it -> Flux.just(memberWithTeamMapper(it)) }
            .asFlow()
    }

    private fun memberWithTeamMapper(row: MutableMap<String, Any>): Member {
        val member = memberMapper(row)
        if (row["team_id"] != null) {
            val team = teamMapper(row)
            member.team = team
        }
        return member
    }

    private fun teamMapper(row: MutableMap<String, Any>): Team {
        return Team(
            id = row["team_id"] as Long,
            name = row["team_name"] as String,
            leaderId = row["team_leader_id"] as Long,
            createdAt = row["team_created_at"] as LocalDateTime,
            modifiedAt = row["team_modified_at"] as LocalDateTime
        )
    }

    private fun memberMapper(row: MutableMap<String, Any>): Member {
        return Member(
            id = row["member_id"] as Long,
            name = row["name"] as String,
            teamId = row["team_id"] as Long?,
            createdAt = row["created_at"] as LocalDateTime,
            modifiedAt = row["modified_at"] as LocalDateTime
        )
    }
}
