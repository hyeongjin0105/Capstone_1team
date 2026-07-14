package com.studystream.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "mediaTBL")
class Media(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    var mediaId: Long? = null,

    @Column(name = "post_id", nullable = false)
    var postId: Long = 0,

    @Column(name = "media_type", nullable = false, length = 20)
    var mediaType: String = "",

    @Column(name = "file_name", nullable = false, length = 255)
    var fileName: String = "",

    @Column(name = "file_path", nullable = false, length = 500)
    var filePath: String = "",

    @Column(name = "content_type", length = 100)
    var contentType: String = "",

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)