package com.studystream.config

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import jakarta.servlet.http.HttpSession
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
@Configuration
class GlobalModelConfig {
    @ModelAttribute
    fun addLoginUser(model: Model, session: HttpSession) {
        // 모든 화면에서 로그인 사용자 정보 사용
        model.addAttribute("username", session.getAttribute("username"))
        model.addAttribute("loginUserId", session.getAttribute("userId"))
    }

    @Bean
    fun generativeModel(
        @Value("${"$"}{gemini.api.key}") apiKey: String
    ): GenerativeModel {
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY_HERE") {
            throw IllegalStateException("`application.properties` 파일에 `gemini.api.key`를 설정해 주세요.")
        }

        val config = generationConfig {
            temperature = 0.7f
            topK = 1
            topP = 1f
            maxOutputTokens = 2048
        }

        val safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.ONLY_HIGH),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.ONLY_HIGH),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.ONLY_HIGH),
        )

        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey,
            generationConfig = config,
            safetySettings = safetySettings
        )
    }
}
