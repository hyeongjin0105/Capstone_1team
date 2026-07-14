package com.studystream.service

import com.studystream.domain.User
import com.studystream.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun signup(email: String, username: String, password: String): Boolean {
        if (email.isBlank() || username.isBlank() || password.isBlank()) {
            return false
        }

        if (userRepository.existsByEmail(email)) {
            return false
        }

        val user = User(
            email = email,
            username = username,
            password = password
        )

        userRepository.save(user)
        return true
    }

    fun login(email: String, password: String): User? {
        val user = userRepository.findByEmail(email)

        if (user != null && user.password == password) {
            return user
        }

        return null
    }

    fun findById(userId: Long): User? {
        return userRepository.findById(userId).orElse(null)
    }
}