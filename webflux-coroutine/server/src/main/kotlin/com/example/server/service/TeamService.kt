package com.example.server.service

import com.example.server.dao.Team
import com.example.server.dto.TeamRequest
import com.example.server.repository.MemberRepository
import com.example.server.repository.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TeamService(
    private val memberRepository: MemberRepository,
    private val teamRepository: TeamRepository
) {

    suspend fun createTeam(teamRequest: TeamRequest): Team {
        val leader = memberRepository.findById(teamRequest.leaderId) ?: throw RuntimeException("leader not found")
        return teamRepository.save(
            Team(
                name = teamRequest.teamName,
                leaderId = leader.id!!
            )
        )
    }

    @Transactional(readOnly = true)
    suspend fun findTeam(teamId: Long): Team {
        return teamRepository.findById(teamId) ?: throw RuntimeException("team not found")
    }

    @Transactional(readOnly = true)
    suspend fun findOneWithMember(teamId: Long): Team {
        return teamRepository.findWithMemberById(teamId) ?: throw RuntimeException("team not found")
    }

    suspend fun updateTeam(teamId: Long, teamRequest: TeamRequest) {
        val team = teamRepository.findById(teamId) ?: throw RuntimeException("team not found")
        team.name = teamRequest.teamName
        team.leaderId = teamRequest.leaderId
        teamRepository.save(team)
    }

    suspend fun deleteTeam(teamId: Long) {
        teamRepository.deleteById(teamId)
    }

    @Transactional(readOnly = true)
    suspend fun findAll(): Flow<Team> {
        return teamRepository.findAll()
    }

    @Transactional(readOnly = true)
    suspend fun findAllWithLeader(): Flow<Team> {
        return teamRepository.findAllWithLeader()
    }

    @Transactional(readOnly = true)
    suspend fun findAllWithMembers(): Flow<Team> {
        return teamRepository.findAllWithMembers()
    }

    @Transactional(readOnly = true)
    suspend fun findAllWithMembersAndLeader(): Flow<Team> {
        return teamRepository.findAllWithMembersAndLeader()
    }
}
