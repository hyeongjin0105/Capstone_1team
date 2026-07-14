package com.studystream.service

import com.studystream.domain.Post
import com.studystream.repository.PostRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PostService(
    private val postRepository: PostRepository
) {
    fun createPost(userId: Long, title: String, content: String, category: String): Post {
        val post = Post(
            userId = userId,
            title = title,
            content = content,
            category = category
        )

        return postRepository.save(post)
    }

    fun getPostList(): List<Post> {
        return postRepository.findAllByOrderByCreatedAtDesc()
    }

    fun getPost(postId: Long): Post? {
        return postRepository.findById(postId).orElse(null)
    }

    fun updatePost(postId: Long, userId: Long, title: String, content: String, category: String): Boolean {
        val post = getPost(postId) ?: return false

        if (post.userId != userId) {
            return false
        }

        post.title = title
        post.content = content
        post.category = category
        post.updatedAt = LocalDateTime.now()

        postRepository.save(post)
        return true
    }

    fun deletePost(postId: Long, userId: Long): Boolean {
        val post = getPost(postId) ?: return false

        if (post.userId != userId) {
            return false
        }

        postRepository.delete(post)
        return true
    }
}