package com.studystream.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "usersTBL")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    var userId: Long? = null,

    @Column(nullable = false, unique = true, length = 50)
    var email: String = "",

    @Column(nullable = false, length = 20)
    var username: String = "",

    @Column(nullable = false, length = 100)
    var password: String = "",

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)