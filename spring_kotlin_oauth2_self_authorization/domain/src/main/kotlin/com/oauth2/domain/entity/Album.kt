package com.oauth2.domain.entity

import com.oauth2.domain.exception.ResourceNotFoundException
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Album(
    friendId: Long,
    title: String,
    description: String
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    var friendId: Long = friendId
        protected set

    var title: String = title
        protected set

    var description: String = description
        protected set

    companion object {
        fun albumNotFound(albumId: Long): ResourceNotFoundException {
            return ResourceNotFoundException.notFound("album", albumId)
        }
    }
}
