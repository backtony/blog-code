package com.example.server.controller.dto

data class ArticleUpdateCommand(
    val title: String,
    val body: String,
)
