package com.example.kotlinboard.service

import com.example.kotlinboard.domain.Post
import com.example.kotlinboard.exception.PostNotDeletedException
import com.example.kotlinboard.exception.PostNotFoundException
import com.example.kotlinboard.exception.PostNotUpdatedException
import com.example.kotlinboard.repository.PostRepository
import com.example.kotlinboard.service.dto.PostCreateRequestDto
import com.example.kotlinboard.service.dto.PostSearchRequestDto
import com.example.kotlinboard.service.dto.PostUpdateRequestDto
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
class PostServiceTest(
    private val postService: PostService,
    private val postRepository: PostRepository,
) : BehaviorSpec({
    beforeSpec {
        postRepository.saveAll(
            listOf(
                Post(title = "title1", content = "content1", createdBy = "rko1"),
                Post(title = "title12", content = "content1", createdBy = "rko1"),
                Post(title = "title13", content = "content1", createdBy = "rko1"),
                Post(title = "title14", content = "content1", createdBy = "rko1"),
                Post(title = "title15", content = "content1", createdBy = "rko1"),
                Post(title = "title6", content = "content1", createdBy = "rko2"),
                Post(title = "title7", content = "content1", createdBy = "rko2"),
                Post(title = "title8", content = "content1", createdBy = "rko2"),
                Post(title = "title9", content = "content1", createdBy = "rko2"),
                Post(title = "title10", content = "content1", createdBy = "rko2")
            )
        )
    }

    given("게시글 생성 시") {
        When("게시글 요청이 정상적으로 들어오면") {
            val postId = postService.createPost(
                PostCreateRequestDto(
                    title = "title",
                    content = "content",
                    createdBy = "rko"
                )
            )
            then("게시글이 정상적으로 생성됨을 확인") {
                postId shouldBeGreaterThan 0L
                val post = postRepository.findByIdOrNull(postId)
                post shouldNotBe null
                post?.title shouldBe "title"
                post?.content shouldBe "content"
                post?.createdBy shouldBe "rko"
            }
        }
    }

    given("게시글 수정 시") {
        val saved = postRepository.save(Post(title = "title", content = "content", createdBy = "rko"))
        When("정상 수정시") {
            val updatedId = postService.updatePost(
                saved.id,
                PostUpdateRequestDto(
                    title = "updated title",
                    content = "updated content",
                    updatedBy = "rko"
                )
            )
            then("게시글이 정상적으로 수정됨을 확인") {
                saved.id shouldBe updatedId
                val updated = postRepository.findByIdOrNull(updatedId)
                updated shouldNotBe null
                updated?.title shouldBe "updated title"
                updated?.content shouldBe "updated content"
            }
        }
        When("게시글이 없을 때") {
            then("게시글을 찾을 수 없다는 예외가 발생") {
                shouldThrow<PostNotFoundException> {
                    postService.updatePost(
                        9999L,
                        PostUpdateRequestDto(
                            title = "updated title",
                            content = "updated content",
                            updatedBy = "update rko"
                        )
                    )
                }
            }
        }
        When("작성자가 동일하지 않으면") {
            then("수정할 수 없는 게시물입니다. 예외가 발생") {
                shouldThrow<PostNotUpdatedException> {
                    postService.updatePost(
                        1L,
                        PostUpdateRequestDto(
                            title = "updated title",
                            content = "updated content",
                            updatedBy = "update rko"
                        )
                    )
                }
            }
        }
    }

    given("게시글 삭제시") {
        val saved = postRepository.save(Post(title = "title", content = "content", createdBy = "rko"))
        When("정상 삭제시") {
            val postId = postService.deletePost(saved.id, "rko")
            then("게시글이 정상적으로 삭제됨을 확인") {
                postId shouldBe saved.id
                postRepository.findByIdOrNull(postId) shouldBe null
            }
        }
        When("작성자가 동일하지 않으면") {
            val saved2 = postRepository.save(Post(title = "title", content = "content", createdBy = "rko"))
            then("삭제할 수 없는 게시물입니다. 예외가 발생") {
                shouldThrow<PostNotDeletedException> { postService.deletePost(saved2.id, "rko2") }
            }
        }
    }

    given("게시글 상세조회시") {
        val saved = postRepository.save(Post(title = "title", content = "content", createdBy = "rko"))
        When("정상 조회시") {
            val post = postService.getPost(saved.id)
            then("게시글이 정상적으로 조회됨을 확인") {
                post.id shouldBe saved.id
                post.title shouldBe "title"
                post.content shouldBe "content"
                post.createdBy shouldBe "rko"
            }
        }
        When("게시글이 없을 때") {
            then("게시글을 찾을 수 없다는 예외가 발생") {
                shouldThrow<PostNotFoundException> { postService.getPost(9999L) }
            }
        }
    }

    given("게시글 목록조회시") {
        When("정상 조회시") {
            val postPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto())
            then("게시글 페이지가 반환된다") {
                postPage.number shouldBe 0
                postPage.size shouldBe 5
                postPage.content.size shouldBe 5
                postPage.content[0].title shouldContain "title"
                postPage.content[0].createdBy shouldContain "rko"
            }
        }
        When("제목으로 검색") {
            val postPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(title = "title1"))
            then("제목에 해당하는 게시글이 반환된다") {
                postPage.number shouldBe 0
                postPage.size shouldBe 5
                postPage.content.size shouldBe 5
                postPage.content[0].title shouldContain "title1"
                postPage.content[0].createdBy shouldContain "rko"
            }
        }
        When("작성자로 검색") {
            val postPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(createdBy = "rko2"))
            then("작성자에 해당하는 개시글이 반환된다") {
                postPage.number shouldBe 0
                postPage.size shouldBe 5
                postPage.content.size shouldBe 5
                postPage.content[0].title shouldContain "title"
                postPage.content[0].createdBy shouldBe "rko2"
            }
        }
    }
})
