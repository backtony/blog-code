package com.example.server.controller

import com.example.server.dao.Member
import com.example.server.dto.MemberRequest
import com.example.server.service.MemberService
import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/member")
class MemberController(
    private val memberService: MemberService
) {

    @PostMapping
    suspend fun createMember(@RequestBody memberRequest: MemberRequest): Member {
        return memberService.createMember(memberRequest)
    }

    @GetMapping("/{id}")
    suspend fun findOne(@PathVariable id: Long): Member {
        return memberService.findMember(id)
    }

    @GetMapping("/with-team/{id}")
    suspend fun findOneWithTeam(@PathVariable id: Long): Member {
        return memberService.findMemberWithTeam(id)
    }

    @PatchMapping("/{id}")
    suspend fun updateMember(@PathVariable id: Long, @RequestBody memberRequest: MemberRequest) {
        return memberService.updateMember(id, memberRequest)
    }

    @DeleteMapping("/{id}")
    suspend fun delete(@PathVariable id: Long) {
        return memberService.delete(id)
    }

    @GetMapping
    suspend fun findMembers(): Flow<Member> {
        return memberService.findMembers()
    }

    @GetMapping("/with-team")
    suspend fun findMembersWithTeam(): Flow<Member> {
        return memberService.findMembersWithTeam()
    }
}
