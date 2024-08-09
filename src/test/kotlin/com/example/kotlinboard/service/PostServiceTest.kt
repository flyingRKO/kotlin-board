package com.example.kotlinboard.service

import com.example.kotlinboard.domain.Comment
import com.example.kotlinboard.domain.Post
import com.example.kotlinboard.domain.Tag
import com.example.kotlinboard.exception.PostNotDeletedException
import com.example.kotlinboard.exception.PostNotFoundException
import com.example.kotlinboard.exception.PostNotUpdatedException
import com.example.kotlinboard.repository.CommentRepository
import com.example.kotlinboard.repository.PostRepository
import com.example.kotlinboard.repository.TagRepository
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
    private val commentRepository: CommentRepository,
    private val tagRepository: TagRepository,
    private val likeService: LikeService,
) : BehaviorSpec({
    beforeSpec {
        postRepository.saveAll(
            listOf(
                Post(title = "title1", content = "content1", createdBy = "rko1", tags = listOf("tag1", "tag2")),
                Post(title = "title12", content = "content1", createdBy = "rko1", tags = listOf("tag1", "tag2")),
                Post(title = "title13", content = "content1", createdBy = "rko1", tags = listOf("tag1", "tag2")),
                Post(title = "title14", content = "content1", createdBy = "rko1", tags = listOf("tag1", "tag2")),
                Post(title = "title15", content = "content1", createdBy = "rko1", tags = listOf("tag1", "tag2")),
                Post(title = "title6", content = "content1", createdBy = "rko2", tags = listOf("tag1", "tag5")),
                Post(title = "title7", content = "content1", createdBy = "rko2", tags = listOf("tag1", "tag5")),
                Post(title = "title8", content = "content1", createdBy = "rko2", tags = listOf("tag1", "tag5")),
                Post(title = "title9", content = "content1", createdBy = "rko2", tags = listOf("tag1", "tag5")),
                Post(title = "title10", content = "content1", createdBy = "rko2", tags = listOf("tag1", "tag5"))
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
        When("태그가 추가되면") {
            val postId = postService.createPost(
                PostCreateRequestDto(
                    title = "제목",
                    content = "내용",
                    createdBy = "rko",
                    tags = listOf("tag1", "tag2")
                )
            )
            then("태그가 정상적으로 추가됨을 확인") {
                val tags = tagRepository.findByPostId(postId)
                tags.size shouldBe 2
                tags[0].name shouldBe "tag1"
                tags[1].name shouldBe "tag2"
            }
        }
    }

    given("게시글 수정 시") {
        val saved = postRepository.save(
            Post(title = "title", content = "content", createdBy = "rko", tags = listOf("tag1", "tag2"))
        )
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
        When("태그가 수정되었을 때") {
            val updatedId = postService.updatePost(
                saved.id,
                PostUpdateRequestDto(
                    title = "updated title",
                    content = "updated content",
                    updatedBy = "rko",
                    tags = listOf("tag1", "tag2", "tag3")
                )
            )
            then("정상적으로 수정됨을 확인") {
                val tags = tagRepository.findByPostId(updatedId)
                tags.size shouldBe 3
                tags[2].name shouldBe "tag3"
            }
            then("태그 순서가 변경되었을 때 정상적으로 변경됨을 확인") {
                postService.updatePost(
                    saved.id,
                    PostUpdateRequestDto(
                        title = "updated title",
                        content = "updated content",
                        updatedBy = "rko",
                        tags = listOf("tag3", "tag2", "tag1")
                    )
                )
                val tags = tagRepository.findByPostId(updatedId)
                tags.size shouldBe 3
                tags[2].name shouldBe "tag1"
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
        tagRepository.saveAll(
            listOf(
                Tag(name = "tag1", post = saved, createdBy = "rko"),
                Tag(name = "tag2", post = saved, createdBy = "rko"),
                Tag(name = "tag3", post = saved, createdBy = "rko")
            )
        )
        likeService.createLike(saved.id, "rko")
        likeService.createLike(saved.id, "rko1")
        likeService.createLike(saved.id, "rko2")
        When("정상 조회시") {
            val post = postService.getPost(saved.id)
            then("게시글이 정상적으로 조회됨을 확인") {
                post.id shouldBe saved.id
                post.title shouldBe "title"
                post.content shouldBe "content"
                post.createdBy shouldBe "rko"
            }
            then("태그가 정상적으로 조회됨을 확인") {
                post.tags.size shouldBe 3
                post.tags[0] shouldBe "tag1"
                post.tags[1] shouldBe "tag2"
                post.tags[2] shouldBe "tag3"
            }
            then("좋아요 개수가 정상적으로 조회됨을 확인") {
                post.likeCount shouldBe 3
            }
        }
        When("게시글이 없을 때") {
            then("게시글을 찾을 수 없다는 예외가 발생") {
                shouldThrow<PostNotFoundException> { postService.getPost(9999L) }
            }
        }
        When("댓글 추가시") {
            commentRepository.save(Comment(content = "댓글1", post = saved, createdBy = "댓글 작성자"))
            commentRepository.save(Comment(content = "댓글2", post = saved, createdBy = "댓글 작성자"))
            commentRepository.save(Comment(content = "댓글3", post = saved, createdBy = "댓글 작성자"))
            val post = postService.getPost(saved.id)
            then("댓글이 포함된 게시글이 반환된다") {
                post.comments.size shouldBe 3
                post.comments[0].content shouldBe "댓글1"
                post.comments[1].content shouldBe "댓글2"
                post.comments[2].content shouldBe "댓글3"
                post.comments[0].createdBy shouldBe "댓글 작성자"
                post.comments[1].createdBy shouldBe "댓글 작성자"
                post.comments[2].createdBy shouldBe "댓글 작성자"
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
            then("첫번째 태그가 함께 조회됨을 확인한다") {
                postPage.content.forEach {
                    it.firstTag shouldBe "tag1"
                }
            }
        }
        When("태그로 검색") {
            val postPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(tag = "tag5"))
            then("태그에 해당하는 게시글이 반환된다") {
                postPage.number shouldBe 0
                postPage.size shouldBe 5
                postPage.content.size shouldBe 5
                postPage.content[0].title shouldContain "title10"
                postPage.content[1].title shouldContain "title9"
                postPage.content[2].title shouldContain "title8"
                postPage.content[3].title shouldContain "title7"
                postPage.content[4].title shouldContain "title6"
            }
        }
        When("좋아요가 2개 추가되었을 때") {
            val postPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(tag = "tag5"))
            postPage.content.forEach {
                likeService.createLike(it.id, "rko1")
                likeService.createLike(it.id, "rko2")
            }
            val likedPostPage = postService.findPageBy(PageRequest.of(0, 5), PostSearchRequestDto(tag = "tag5"))
            then("좋아요 개수가 정상적으로 조회됨을 확인") {
                likedPostPage.content.forEach {
                    it.likeCount shouldBe 2
                }
            }
        }
    }
})
