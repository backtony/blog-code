package com.example.client.service

import com.example.client.adapter.MemberApi
import com.example.client.dto.MemberRequest
import com.example.server.dao.Member
import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MemberService(
    private val memberApi: MemberApi
) {
    suspend fun createMember(memberRequest: MemberRequest): Member {
        return memberApi.save(memberRequest)
    }

    @Transactional(readOnly = true)
    suspend fun findMember(memberId: Long): Member {
        return memberApi.findById(memberId)
    }

    @Transactional(readOnly = true)
    suspend fun findMemberWithTeam(memberId: Long): Member {
        return memberApi.findWithTeamById(memberId) ?: throw RuntimeException("member not found")
    }

    suspend fun updateMember(memberId: Long, memberRequest: MemberRequest) {
        memberApi.updateMember(memberId, memberRequest)
    }

    suspend fun delete(memberId: Long) {
        memberApi.deleteById(memberId)
    }

    @Transactional(readOnly = true)
    suspend fun findMembers(): Flow<Member> {
        return memberApi.findAll()
    }

    @Transactional(readOnly = true)
    suspend fun findMembersWithTeam(): Flow<Member> {
        return memberApi.findAllWithTeam()
    }
}
