package com.example.server.repository

import com.example.server.dao.Member
import com.example.server.dao.Team
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.LocalDateTime

// asFlow로 반환한 경우 예외가 발생함.
// service 단에서 asFlow로 변환시키면 문제 없음
// repository단에서 해결하고 싶다면 asFlow.toList로 반환해야함
// repository단에서 asFlow를 붙이면 아래와 같은 예외가 발생하는데 이유를 찾지 못함
// class reactor.core.publisher.FluxOnAssembly cannot be cast to class kotlinx.coroutines.flow.Flow
// service 단에서 asFlow를 붙이면 문제가 없으나 repository단에서 asFlow를 붙이면 아래 예외가 발생하는데 이유가 무엇일까..?
interface TeamRepositoryCustom {
    suspend fun findWithMemberById(teamId: Long): Team?
    suspend fun findAllWithMembers(): Flux<Team>
    suspend fun findAllWithLeader(): Flux<Team>
    suspend fun findAllWithMembersAndLeader(): Flux<Team>
}

interface TeamRepository : CoroutineCrudRepository<Team, Long>, TeamRepositoryCustom

@Repository
class TeamRepositoryCustomImpl(
    private val client: DatabaseClient
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

    override suspend fun findAllWithMembers(): Flux<Team> {
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
            .map { teamWithMemberMapper(it) }
//            .asFlow()
//            .toList()
    }

    override suspend fun findAllWithLeader(): Flux<Team> {
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
            .map { teamWithLeaderMapper(it) }
//            .asFlow()
//            .toList()
    }

    override suspend fun findAllWithMembersAndLeader(): Flux<Team> {
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
            .map { teamWithLeaderAndMembersMapper(it) }
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
