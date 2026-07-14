package com.studystream.controller

import com.studystream.service.SearchService
import com.studystream.service.UserService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class SearchController(
    private val searchService: SearchService,
    private val userService: UserService
) {
    @GetMapping("/search")
    fun search(
        @RequestParam keyword: String,
        model: Model
    ): String {
        val posts = searchService.searchPosts(keyword)

        val postRows = posts.map { post ->
            val writer = userService.findById(post.userId)

            mapOf(
                "post" to post,
                "writerName" to (writer?.username ?: "알 수 없음")
            )
        }

        model.addAttribute("postRows", postRows)
        model.addAttribute("keyword", keyword)

        return "search-result"
    }
}