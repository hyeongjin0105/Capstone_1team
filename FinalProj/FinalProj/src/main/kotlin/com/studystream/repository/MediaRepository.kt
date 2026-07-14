package com.studystream.repository

import com.studystream.domain.Media
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional

interface MediaRepository : JpaRepository<Media, Long> {
    fun findByPostId(postId: Long): List<Media>
    fun findByMediaIdAndPostId(mediaId: Long, postId: Long): Media?

    @Transactional
    fun deleteByPostId(postId: Long)
}
