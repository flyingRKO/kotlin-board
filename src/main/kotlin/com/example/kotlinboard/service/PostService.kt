package com.example.kotlinboard.service

import com.example.kotlinboard.exception.PostNotDeletedException
import com.example.kotlinboard.exception.PostNotFoundException
import com.example.kotlinboard.repository.PostRepository
import com.example.kotlinboard.repository.TagRepository
import com.example.kotlinboard.service.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PostService(
    private val postRepository: PostRepository,
    private val likeService: LikeService,
    private val tagRepository: TagRepository,
) {
    @Transactional
    fun createPost(requestDto: PostCreateRequestDto): Long {
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

    fun getPost(id: Long): PostDetailResponseDto {
        val likeCount = likeService.countLike(id)
        return postRepository.findByIdOrNull(id)?.toDetailResponseDto(likeCount) ?: throw PostNotFoundException()
    }

    fun findPageBy(pageRequest: Pageable, postSearchRequestDto: PostSearchRequestDto): Page<PostSummaryResponseDto> {
        postSearchRequestDto.tag?.let {
            return tagRepository.findPageBy(pageRequest, it).toSummaryResponseDto(
                likeService::countLike
            )
        }
        return postRepository.findPageBy(pageRequest, postSearchRequestDto).toSummaryResponseDto(likeService::countLike)
    }
}
