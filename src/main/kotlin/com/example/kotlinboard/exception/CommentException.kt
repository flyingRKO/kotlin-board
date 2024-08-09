package com.example.kotlinboard.exception

open class CommentException(message: String) : RuntimeException(message)

class CommentNotUpdateException : CommentException("댓글을 수정할 수 없습니다.")

class CommentNotFoundException : CommentException("댓글을 찾을 수 없습니다.")

class CommentNotDeleteException : CommentException("댓글을 삭제할 수 없습니다.")
