package com.example.kotlinboard.service

import com.example.kotlinboard.exception.CommentNotDeleteException
import com.example.kotlinboard.exception.CommentNotFoundException
import com.example.kotlinboard.exception.PostNotFoundException
import com.example.kotlinboard.repository.CommentRepository
import com.example.kotlinboard.repository.PostRepository
import com.example.kotlinboard.service.dto.CommentCreateRequestDto
import com.example.kotlinboard.service.dto.CommentUpdateRequestDto
import com.example.kotlinboard.service.dto.toEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
) {
    @Transactional
    fun createComment(postId: Long, commentCreateRequestDto: CommentCreateRequestDto): Long {
        val post = postRepository.findByIdOrNull(postId) ?: throw PostNotFoundException()
        return commentRepository.save(commentCreateRequestDto.toEntity(post)).id
    }

    @Transactional
    fun updateComment(id: Long, updateRequestDto: CommentUpdateRequestDto): Long {
        val comment = commentRepository.findByIdOrNull(id) ?: throw CommentNotFoundException()
        comment.update(updateRequestDto)
        return comment.id
    }

    @Transactional
    fun deleteComment(id: Long, deleteBy: String): Long {
        val comment = commentRepository.findByIdOrNull(id) ?: throw CommentNotFoundException()
        if (comment.createdBy != deleteBy) {
            throw CommentNotDeleteException()
        }
        commentRepository.delete(comment)
        return id
    }
}
