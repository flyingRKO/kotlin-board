package com.example.kotlinboard.controller

import com.example.kotlinboard.controller.dto.PostCreateRequest
import com.example.kotlinboard.controller.dto.PostUpdateRequest
import com.example.kotlinboard.controller.dto.toDto
import com.example.kotlinboard.service.PostService
import org.springframework.web.bind.annotation.*

@RestController
class PostController(
    private val postService: PostService,
) {

    @PostMapping("/posts")
    fun createPost(
        @RequestBody request: PostCreateRequest,
    ): Long {
        return postService.createPost(request.toDto())
    }

    @PostMapping("/posts/{id}")
    fun updatePost(
        @PathVariable id: Long,
        @RequestBody request: PostUpdateRequest,
    ): Long {
        return postService.updatePost(id, request.toDto())
    }

    @DeleteMapping("/posts/{id}")
    fun deletePost(
        @PathVariable id: Long,
        @RequestParam deleteBy: String,
    ): Long {
        return postService.deletePost(id, deleteBy)
    }
}
