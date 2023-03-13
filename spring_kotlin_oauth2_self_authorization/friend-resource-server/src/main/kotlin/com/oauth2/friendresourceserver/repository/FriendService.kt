package com.oauth2.friendresourceserver.repository

import com.oauth2.domain.entity.Friend
import com.oauth2.domain.repository.FriendRepository
import com.oauth2.friendresourceserver.dto.FriendResponse
import com.oauth2.friendresourceserver.dto.FriendSaveRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class FriendService(
    private val friendRepository: FriendRepository
) {

    fun saveFriend(friendSaveRequest: FriendSaveRequest): Long {
        return friendRepository.save(
            Friend(
                name = friendSaveRequest.name,
                age = friendSaveRequest.age
            )
        ).id!!
    }

    @Transactional(readOnly = true)
    fun findFriend(friendId: Long): FriendResponse {
        val friend = friendRepository.findByIdOrNull(friendId)
            ?: throw Friend.friendNotFound(friendId)
        return FriendResponse.from(friend)
    }
}
