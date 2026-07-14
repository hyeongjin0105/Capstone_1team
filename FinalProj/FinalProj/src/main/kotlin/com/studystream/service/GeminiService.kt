package com.studystream.service

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class GeminiService(
    private val generativeModel: GenerativeModel
) {
    private val questionPrompt = """
        You are an AI assistant that generates quiz questions based on the provided lecture transcript.
        Generate a set of multiple-choice questions that cover the key concepts in the text.
        Each question should have one correct answer and three plausible incorrect answers.
        Format the output clearly.
    """.trimIndent()

    private val summaryPrompt = """
        You are an AI assistant that summarizes lecture transcripts.
        Summarize the following text, focusing on the main ideas and key points.
        The summary should be concise and easy to understand.
    """.trimIndent()

    fun generateQuestions(lectureText: String): String {
        return runBlocking {
            val fullPrompt = "$questionPrompt

---

$lectureText"
            val response = generateContent(fullPrompt)
            response.text ?: "Failed to generate questions. Please try again."
        }
    }

    fun summarize(lectureText: String): String {
        return runBlocking {
            val fullPrompt = "$summaryPrompt

---

$lectureText"
            val response = generateContent(fullPrompt)
            response.text ?: "Failed to summarize. Please try again."
        }
    }

    private suspend fun generateContent(prompt: String): GenerateContentResponse {
        val config = generationConfig {
            temperature = 0.7f
            topK = 1
            topP = 1f
            maxOutputTokens = 2048
        }
        return generativeModel.generateContent(prompt, config)
    }
}
