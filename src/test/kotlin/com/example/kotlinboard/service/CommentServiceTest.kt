package com.example.kotlinboard.service

import com.example.kotlinboard.domain.Comment
import com.example.kotlinboard.domain.Post
import com.example.kotlinboard.exception.CommentNotDeleteException
import com.example.kotlinboard.exception.CommentNotUpdateException
import com.example.kotlinboard.exception.PostNotFoundException
import com.example.kotlinboard.repository.CommentRepository
import com.example.kotlinboard.repository.PostRepository
import com.example.kotlinboard.service.dto.CommentCreateRequestDto
import com.example.kotlinboard.service.dto.CommentUpdateRequestDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
class CommentServiceTest(
    private val commentService: CommentService,
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository,
) : BehaviorSpec({
    given("댓글 생성시") {
        val post = postRepository.save(
            Post(
                title = "게시글 제목",
                content = "게시글 내용",
                createdBy = "게시글 작성자"
            )
        )
        When("입력값이 정상적으로 들어오면") {
            val commentId = commentService.createComment(
                post.id,
                CommentCreateRequestDto(
                    content = "댓글 내용",
                    createdBy = "댓글 작성자"
                )
            )
            then("정상적으로 생성됨을 확인") {
                commentId shouldBeGreaterThan 0L
                val comment = commentRepository.findByIdOrNull(commentId)
                comment shouldNotBe null
                comment?.content shouldBe "댓글 내용"
                comment?.createdBy shouldBe "댓글 작성자"
            }
        }
        When("게시글이 존재하지 않으면") {
            then("게시글이 존재하지 않는다는 예외가 발생한다") {
                shouldThrow<PostNotFoundException> {
                    commentService.createComment(
                        9999L,
                        CommentCreateRequestDto(
                            content = "댓글 내용",
                            createdBy = "댓글 작성자"
                        )
                    )
                }
            }
        }
    }

    given("댓글 수정시") {
        val post = postRepository.save(
            Post(
                title = "게시글 제목",
                content = "게시글 내용",
                createdBy = "게시글 작성자"
            )
        )
        val saved = commentRepository.save(Comment("댓글 내용", post, "댓글 작성자"))
        When("입력값이 정상적으로 들어오면") {
            val updateId = commentService.updateComment(
                saved.id,
                CommentUpdateRequestDto(
                    content = "수정된 댓글 내용",
                    updatedBy = "댓글 작성자"
                )
            )
            then("정상적으로 수정됨을 확인") {
                updateId shouldBe saved.id
                val updated = commentRepository.findByIdOrNull(updateId)
                updated shouldNotBe null
                updated?.content shouldBe "수정된 댓글 내용"
                updated?.updatedBy shouldBe "댓글 작성자"
            }
        }
        When("작성자와 수정자가 다르면") {
            then("수정할 수 없는 댓글 예외가 발생") {
                shouldThrow<CommentNotUpdateException> {
                    commentService.updateComment(
                        saved.id,
                        CommentUpdateRequestDto(
                            content = "수정된 댓글 내용",
                            updatedBy = "수정된 댓글 작성자"
                        )
                    )
                }
            }
        }
    }
    given("댓글 삭제시") {
        val post = postRepository.save(
            Post(
                title = "게시글 제목",
                content = "게시글 내용",
                createdBy = "게시글 작성자"
            )
        )

        val saved = commentRepository.save(Comment("댓글 내용", post, "댓글 작성자"))
        val saved2 = commentRepository.save(Comment("댓글 내용2", post, "댓글 작성자2"))

        When("입력값이 정상적으로 들어오면") {
            val commentId = commentService.deleteComment(saved.id, "댓글 작성자")
            then("정상적으로 삭제됨을 확인") {
                commentId shouldBe saved.id
                commentRepository.findByIdOrNull(saved.id) shouldBe null
            }
        }
        When("작성자와 삭제자가 다르면") {
            then("삭제할 수 없는 댓글 예외가 발생") {
                shouldThrow<CommentNotDeleteException> {
                    commentService.deleteComment(saved2.id, "댓글 삭제자")
                }
            }
        }
    }
})
