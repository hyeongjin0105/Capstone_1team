package com.studystream.repository

import com.studystream.domain.Post
import org.springframework.data.jpa.repository.JpaRepository

interface PostRepository : JpaRepository<Post, Long> {
    fun findAllByOrderByCreatedAtDesc(): List<Post>

    fun findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByCreatedAtDesc(
        titleKeyword: String,
        contentKeyword: String
    ): List<Post>
}