package com.example.server.dto

data class MemberRequest(
    val name: String,
    val teamId: Long?
)
