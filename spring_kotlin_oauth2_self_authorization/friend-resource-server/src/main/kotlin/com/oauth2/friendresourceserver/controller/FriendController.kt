package com.oauth2.friendresourceserver.controller

import com.oauth2.friendresourceserver.dto.FriendResponse
import com.oauth2.friendresourceserver.dto.FriendSaveRequest
import com.oauth2.friendresourceserver.repository.FriendService
import org.springframework.web.bind.annotation.*

@RestController
class FriendController(
    private val friendService: FriendService
) {

    @GetMapping("/friends/{friendId}")
    fun findFriend(@PathVariable friendId: Long): FriendResponse {
        return friendService.findFriend(friendId)
    }

    @PostMapping("/friends")
    fun saveFriend(@RequestBody friendSaveRequest: FriendSaveRequest): Long {
        return friendService.saveFriend(friendSaveRequest)
    }
}
