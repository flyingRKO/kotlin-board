package com.example.kotlinboard.domain

import com.example.kotlinboard.exception.CommentNotUpdateException
import com.example.kotlinboard.service.dto.CommentUpdateRequestDto
import jakarta.persistence.*

@Entity
class Comment(
    content: String,
    post: Post,
    createdBy: String,
) : BaseEntity(createdBy = createdBy) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    var content: String = content
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    var post: Post = post
        protected set

    fun update(updateRequestDto: CommentUpdateRequestDto) {
        if (updateRequestDto.updatedBy != this.createdBy) {
            throw CommentNotUpdateException()
        }
        this.content = updateRequestDto.content
        super.updatedBy(updateRequestDto.updatedBy)
    }
}
