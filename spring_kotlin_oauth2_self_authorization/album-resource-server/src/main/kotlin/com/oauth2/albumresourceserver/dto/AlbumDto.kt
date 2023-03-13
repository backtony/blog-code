package com.oauth2.albumresourceserver.dto

import com.oauth2.domain.entity.Album

data class AlbumSaveRequest(
    var friendName: String,
    var friendAge: Int,
    var albumTitle: String,
    var albumDescription: String
)

data class AlbumResponse(
    val id: Long,
    val friendId: Long,
    val title: String,
    val description: String
) {
    companion object {
        fun from(album: Album) = AlbumResponse(
            id = album.id!!,
            friendId = album.friendId!!,
            title = album.title,
            description = album.description
        )
    }
}
