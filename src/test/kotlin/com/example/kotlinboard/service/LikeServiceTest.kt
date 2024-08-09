package com.example.kotlinboard.service

import com.example.kotlinboard.domain.Post
import com.example.kotlinboard.exception.PostNotFoundException
import com.example.kotlinboard.repository.LikeRepository
import com.example.kotlinboard.repository.PostRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
class LikeServiceTest(
    private val likeService: LikeService,
    private val likeRepository: LikeRepository,
    private val postRepository: PostRepository,
) : BehaviorSpec({
    given("좋아요 생성시") {
        val saved = postRepository.save(Post("rko", "title", "content"))
        When("입력값이 정상적으로 들어오면") {
            val likeId = likeService.createLike(saved.id, "rko")
            Then("좋아요가 생성된다") {
                val like = likeRepository.findByIdOrNull(likeId)
                like shouldNotBe null
                like?.createdBy shouldBe "rko"
            }
        }
        When("게시글이 존재하지 않으면") {
            then("존재하지 않는 게시글 예외 발생") {
                shouldThrow<PostNotFoundException> { likeService.createLike(9999L, "rko") }
            }
        }
    }
})
