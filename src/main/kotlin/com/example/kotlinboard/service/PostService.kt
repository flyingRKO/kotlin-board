package com.example.kotlinboard.service

import com.example.kotlinboard.exception.PostNotDeletedException
import com.example.kotlinboard.exception.PostNotFoundException
import com.example.kotlinboard.repository.PostRepository
import com.example.kotlinboard.service.dto.PostCreateRequestDto
import com.example.kotlinboard.service.dto.PostUpdateRequestDto
import com.example.kotlinboard.service.dto.toEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PostService(
    private val postRepository: PostRepository,
) {
    @Transactional
    fun createPost(requestDto: PostCreateRequestDto) : Long {
        return postRepository.save(requestDto.toEntity()).id
    }

    @Transactional
    fun updatePost(id: Long, requestDto: PostUpdateRequestDto): Long {
        val post = postRepository.findByIdOrNull(id) ?: throw PostNotFoundException()
        post.update(requestDto)
        return id
    }

    @Transactional
    fun deletePost(id: Long, deleteBy: String): Long {
        val post = postRepository.findByIdOrNull(id) ?: throw PostNotDeletedException()
        if (post.createdBy != deleteBy) throw PostNotDeletedException()
        postRepository.delete(post)
        return id
    }

}
