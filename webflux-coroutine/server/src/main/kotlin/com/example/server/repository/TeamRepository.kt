package com.example.server.repository

import com.example.server.dao.Member
import com.example.server.dao.Team
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.LocalDateTime

interface TeamRepositoryCustom {
    suspend fun findWithMemberById(teamId: Long): Team?

    // flow(비동기적인 데이터 스트림)를 반환하는 함수는 suspend 키워드를 붙이지 않는다.
    // cold stream으로 flow가 collect되는 시점에 실행된다.
    // collect 하는 곳에서는 suspend 키워드가 붙어있어야 한다.(코루틴 스코프나 suspend 함수 내에서 collect되어야 한다.)
    fun findAllWithMembers(): Flow<Team>
    fun findAllWithLeader(): Flow<Team>
    fun findAllWithMembersAndLeader(): Flow<Team>
}

interface TeamRepository : CoroutineCrudRepository<Team, Long>, TeamRepositoryCustom

@Repository
class TeamRepositoryCustomImpl(
    private val client: DatabaseClient,
) : TeamRepositoryCustom {

    override suspend fun findWithMemberById(teamId: Long): Team? {
        val sql = """
            SELECT T.*,
            M.member_id AS member_id,
            M.name AS member_name,
            M.created_at AS member_created_at,
            M.modified_at AS member_modified_at
            FROM Team T
            LEFT JOIN Member M ON T.team_id = M.team_id
            WHERE T.team_id = :teamId
        """.trimIndent()

        return client.sql(sql)
            .bind("teamId", teamId)
            .fetch()
            .all()
            .bufferUntilChanged {
                it["team_id"]
            }
            .map { teamWithMemberMapper(it) }
            .asFlow()
            .firstOrNull()
    }

    override fun findAllWithMembers(): Flow<Team> {
        val sql = """
            SELECT T.*,
            M.member_id AS member_id,
            M.name AS member_name,
            M.created_at AS member_created_at,
            M.modified_at AS member_modified_at
            FROM Team T
            LEFT JOIN Member M ON T.team_id = M.team_id
        """.trimIndent()

        return client.sql(sql)
            .fetch()
            .all()
            .bufferUntilChanged {
                it["team_id"]
            }
            .flatMap { it -> Flux.just(teamWithMemberMapper(it)) }
            .asFlow()
    }

    override fun findAllWithLeader(): Flow<Team> {
        val sql = """
            SELECT T.*,            
            L.name AS leader_name,
            L.team_id AS leader_team_id,
            L.created_at AS leader_created_at,
            L.modified_at AS leader_modified_at
            FROM Team T
            LEFT JOIN Member L ON T.leader_id = L.member_id
        """.trimIndent()

        return client.sql(sql)
            .fetch()
            .all()
            .flatMap { it -> Flux.just(teamWithLeaderMapper(it)) }
            .asFlow()
    }

    override fun findAllWithMembersAndLeader(): Flow<Team> {
        val sql = """
            SELECT T.*,
            M.member_id AS member_id,
            M.name AS member_name,
            M.created_at AS member_created_at,
            M.modified_at AS member_modified_at,
            L.name AS leader_name,
            L.created_at AS leader_created_at,
            L.modified_at AS leader_modified_at
            FROM Team T
            LEFT JOIN Member M ON T.team_id = M.team_id
            LEFT JOIN Member L ON T.leader_id = L.member_id
        """.trimIndent()

        return client.sql(sql)
            .fetch()
            .all()
            .bufferUntilChanged {
                it["team_id"]
            }
            .flatMap { it -> Flux.just(teamWithLeaderAndMembersMapper(it)) }
            .asFlow()
    }

    private fun teamWithLeaderAndMembersMapper(it: MutableList<MutableMap<String, Any>>): Team {
        val team = teamMapper(it[0])
        team.leader = leaderMapper(it[0])
        if (it[0]["member_id"] != null) {
            team.members = it.stream()
                .map { memberMapper(it) }
                .toList()
        }
        return team
    }

    private fun teamWithMemberMapper(it: MutableList<MutableMap<String, Any>>): Team {
        val team = teamMapper(it[0])
        if (it[0]["member_id"] != null) {
            team.members = it.stream()
                .map { memberMapper(it) }
                .toList()
        }
        return team
    }

    private fun teamWithLeaderMapper(it: MutableMap<String, Any>): Team {
        val team = teamMapper(it)
        team.leader = leaderMapper(it)
        return team
    }

    private fun teamMapper(it: MutableMap<String, Any>): Team {
        return Team(
            id = it["team_id"] as Long,
            name = it["name"] as String,
            leaderId = it["leader_id"] as Long,
            createdAt = it["created_at"] as LocalDateTime,
            modifiedAt = it["modified_at"] as LocalDateTime
        )
    }

    private fun memberMapper(it: MutableMap<String, Any>): Member {
        return Member(
            id = it["member_id"] as Long,
            name = it["member_name"] as String,
            teamId = it["team_id"] as Long,
            createdAt = it["created_at"] as LocalDateTime,
            modifiedAt = it["modified_at"] as LocalDateTime
        )
    }

    private fun leaderMapper(it: MutableMap<String, Any>): Member {
        return Member(
            id = it["leader_id"] as Long,
            name = it["leader_name"] as String,
            teamId = it["leader_team_id"] as Long?,
            createdAt = it["created_at"] as LocalDateTime,
            modifiedAt = it["modified_at"] as LocalDateTime
        )
    }
}
