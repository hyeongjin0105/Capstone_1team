package com.studystream.controller

import com.studystream.service.UserService
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class UserController(
    private val userService: UserService
) {
    @GetMapping("/")
    fun home(): String {
        return "redirect:/posts"
    }

    @GetMapping("/signup")
    fun signupPage(): String {
        return "signup"
    }

    @PostMapping("/signup")
    fun signup(
        @RequestParam email: String,
        @RequestParam username: String,
        @RequestParam password: String,
        model: Model
    ): String {
        val result = userService.signup(email, username, password)

        if (!result) {
            model.addAttribute("error", "회원가입에 실패했습니다. 이메일 중복 또는 입력값을 확인하세요.")
            return "signup"
        }

        return "redirect:/login"
    }

    @GetMapping("/login")
    fun loginPage(): String {
        return "login"
    }

    @PostMapping("/login")
    fun login(
        @RequestParam email: String,
        @RequestParam password: String,
        session: HttpSession,
        model: Model
    ): String {
        val user = userService.login(email, password)

        if (user == null) {
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.")
            return "login"
        }

        session.setAttribute("userId", user.userId)
        session.setAttribute("username", user.username)

        return "redirect:/posts"
    }

    @GetMapping("/logout")
    fun logout(session: HttpSession): String {
        session.invalidate()
        return "redirect:/login"
    }
}