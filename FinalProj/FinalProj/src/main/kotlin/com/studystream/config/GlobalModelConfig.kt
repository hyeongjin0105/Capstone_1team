package com.studystream.config

import jakarta.servlet.http.HttpSession
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class GlobalModelConfig {
    @ModelAttribute
    fun addLoginUser(model: Model, session: HttpSession) {
        // 모든 화면에서 로그인 사용자 정보 사용
        model.addAttribute("username", session.getAttribute("username"))
        model.addAttribute("loginUserId", session.getAttribute("userId"))
    }
}
