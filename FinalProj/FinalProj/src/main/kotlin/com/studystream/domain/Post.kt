package com.studystream.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "postsTBL")
class Post(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    var postId: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0,

    @Column(nullable = false, length = 100)
    var title: String = "",

    @Column(columnDefinition = "TEXT")
    var content: String = "",

    @Column(length = 30)
    var category: String = "",

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)