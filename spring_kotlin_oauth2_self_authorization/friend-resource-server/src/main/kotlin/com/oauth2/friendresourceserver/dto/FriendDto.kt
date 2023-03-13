package com.oauth2.friendresourceserver.dto

import com.oauth2.domain.entity.Friend

data class FriendSaveRequest(
    val name: String,
    val age: Int
)

data class FriendResponse(
    val id: Long,
    val name: String,
    val age: Int
) {
    companion object {
        fun from(friend: Friend) = FriendResponse(
            id = friend.id!!,
            name = friend.name,
            age = friend.age
        )
    }
}
