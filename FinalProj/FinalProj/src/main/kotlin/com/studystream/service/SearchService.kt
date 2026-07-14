package com.studystream.service

import com.studystream.domain.Post
import com.studystream.repository.PostRepository
import org.springframework.stereotype.Service

@Service
class SearchService(
    private val postRepository: PostRepository
) {
    fun searchPosts(keyword: String): List<Post> {
        if (keyword.isBlank()) {
            return postRepository.findAllByOrderByCreatedAtDesc()
        }

        return postRepository
            .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByCreatedAtDesc(
                keyword,
                keyword
            )
    }
}