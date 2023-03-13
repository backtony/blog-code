package com.oauth2.domain.entity

import com.oauth2.domain.exception.ResourceNotFoundException
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Friend(
    name: String,
    age: Int
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    var name: String = name
        protected set

    var age: Int = age
        protected set

    companion object {
        fun friendNotFound(friendId: Long): ResourceNotFoundException {
            return ResourceNotFoundException.notFound("friend", friendId)
        }
    }
}
