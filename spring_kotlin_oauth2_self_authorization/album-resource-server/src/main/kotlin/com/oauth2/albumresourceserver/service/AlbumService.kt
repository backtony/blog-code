package com.oauth2.albumresourceserver.service

import com.oauth2.albumresourceserver.dto.AlbumResponse
import com.oauth2.albumresourceserver.dto.AlbumSaveRequest
import com.oauth2.albumresourceserver.dto.FriendSaveRequest
import com.oauth2.domain.entity.Album
import com.oauth2.domain.repository.AlbumRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

// restTemplate docs : https://www.baeldung.com/rest-template#use-post-to-create-a-resource
@Service
class AlbumService(
    private val albumRepository: AlbumRepository,
    private val restTemplate: RestTemplate
) {

    fun save(albumSaveRequest: AlbumSaveRequest): Long {
        val request = HttpEntity(
            FriendSaveRequest(
                name = albumSaveRequest.friendName,
                age = albumSaveRequest.friendAge
            )
        )
        val friendId = restTemplate.postForObject(
            "http://localhost:8082/friends",
            request,
            Long::class.java
        )

        return albumRepository.save(
            Album(
                friendId = friendId!!,
                title = albumSaveRequest.albumTitle,
                description = albumSaveRequest.albumDescription
            )
        ).id!!
    }

    fun findAlbum(albumId: Long): AlbumResponse {
        val album = albumRepository.findByIdOrNull(albumId) ?: throw Album.albumNotFound(albumId)
        return AlbumResponse.from(album)
    }
}
