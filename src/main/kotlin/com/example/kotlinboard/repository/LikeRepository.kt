package com.example.kotlinboard.repository

import com.example.kotlinboard.domain.Like
import org.springframework.data.jpa.repository.JpaRepository

interface LikeRepository : JpaRepository<Like, Long>
