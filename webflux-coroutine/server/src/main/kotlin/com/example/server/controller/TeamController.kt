package com.example.server.controller

import com.example.server.dao.Team
import com.example.server.dto.TeamRequest
import com.example.server.service.TeamService
import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/team")
class TeamController(
    private val teamService: TeamService
) {

    @PostMapping
    suspend fun createTeam(@RequestBody teamRequest: TeamRequest): Team {
        return teamService.createTeam(teamRequest)
    }

    @GetMapping("/{id}")
    suspend fun findOne(@PathVariable id: Long): Team {
        return teamService.findTeam(id)
    }

    @GetMapping("/with-member/{id}")
    suspend fun findOneWithMember(@PathVariable id: Long): Team {
        return teamService.findOneWithMember(id)
    }

    @GetMapping
    suspend fun findAll(): Flow<Team> {
        return teamService.findAll()
    }

    @GetMapping("/with-leader")
    suspend fun findAllWithLeader(): Flow<Team> {
        return teamService.findAllWithLeader()
    }

    @GetMapping("/with-member")
    suspend fun findAllWithMembers(): Flow<Team> {
        return teamService.findAllWithMembers()
    }

    @GetMapping("/with-member-leader")
    suspend fun findAllWithMembersAndLeader(): Flow<Team> {
        return teamService.findAllWithMembersAndLeader()
    }

    @PatchMapping("/{id}")
    suspend fun updateTeam(@PathVariable id: Long, @RequestBody teamRequest: TeamRequest) {
        return teamService.updateTeam(id, teamRequest)
    }

    @DeleteMapping("/{id}")
    suspend fun delete(@PathVariable id: Long) {
        return teamService.deleteTeam(id)
    }
}
