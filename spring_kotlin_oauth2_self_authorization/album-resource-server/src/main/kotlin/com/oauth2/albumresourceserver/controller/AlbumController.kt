package com.oauth2.albumresourceserver.controller

import com.oauth2.albumresourceserver.dto.AlbumResponse
import com.oauth2.albumresourceserver.dto.AlbumSaveRequest
import com.oauth2.albumresourceserver.service.AlbumService
import org.springframework.web.bind.annotation.*

@RestController
class AlbumController(
    private val albumService: AlbumService
) {

    @GetMapping("/albums/{albumId}")
    fun findFriend(@PathVariable albumId: Long): AlbumResponse {
        return albumService.findAlbum(albumId)
    }

    @PostMapping("/albums")
    fun saveFriend(@RequestBody albumSaveRequest: AlbumSaveRequest): Long {
        return albumService.save(albumSaveRequest)
    }
}
