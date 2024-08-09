package com.example.kotlinboard.service.dto

import com.example.kotlinboard.domain.Comment
import com.example.kotlinboard.domain.Post

data class CommentCreateRequestDto(
    val content: String,
    val createdBy: String,
)

fun CommentCreateRequestDto.toEntity(post: Post) = Comment(
    content = content,
    createdBy = createdBy,
    post = post
)
