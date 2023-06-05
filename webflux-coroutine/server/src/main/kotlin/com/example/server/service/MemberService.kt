package com.example.server.service

import com.example.server.dao.Member
import com.example.server.dto.MemberRequest
import com.example.server.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository
) {
    suspend fun createMember(memberRequest: MemberRequest): Member {
        return memberRepository.save(Member(name = memberRequest.name))
    }

    @Transactional(readOnly = true)
    suspend fun findMember(memberId: Long): Member {
        return memberRepository.findById(memberId) ?: throw RuntimeException("member not found")
    }

    @Transactional(readOnly = true)
    suspend fun findMemberWithTeam(memberId: Long): Member {
        return memberRepository.findWithTeamById(memberId) ?: throw RuntimeException("member not found")
    }

    suspend fun updateMember(memberId: Long, memberRequest: MemberRequest) {
        val member = memberRepository.findById(memberId) ?: throw RuntimeException("member not found")
        member.name = memberRequest.name
        member.teamId = memberRequest.teamId
        memberRepository.save(member) // dirty checking은 jpa 기능이므로 명시적 save 필요
    }

    suspend fun delete(memberId: Long) {
        memberRepository.deleteById(memberId)
    }

    @Transactional(readOnly = true)
    suspend fun findMembers(): Flow<Member> {
        return memberRepository.findAll()
    }

    @Transactional(readOnly = true)
    suspend fun findMembersWithTeam(): Flow<Member> {
        return memberRepository.findAllWithTeam()
    }
}

