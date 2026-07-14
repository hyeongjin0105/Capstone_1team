package com.studystream.controller

import com.studystream.service.MediaService
import com.studystream.service.PostService
import com.studystream.service.UserService
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Controller
@RequestMapping("/posts")
class PostController(
    private val postService: PostService,
    private val mediaService: MediaService,
    private val userService: UserService
) {
    @GetMapping
    fun postList(model: Model, session: HttpSession): String {
        val posts = postService.getPostList()

        val postRows = posts.map { post ->
            val writer = userService.findById(post.userId)

            mapOf(
                "post" to post,
                "writerName" to (writer?.username ?: "알 수 없음")
            )
        }

        model.addAttribute("postRows", postRows)
        model.addAttribute("username", session.getAttribute("username"))

        return "post-list"
    }

    @GetMapping("/write")
    fun writePage(session: HttpSession): String {
        val userId = session.getAttribute("userId") as Long?

        if (userId == null) {
            return "redirect:/login"
        }

        return "post-write"
    }

    @PostMapping("/write")
    fun writePost(
        @RequestParam title: String,
        @RequestParam content: String,
        @RequestParam category: String,
        @RequestParam("mediaFiles", required = false) mediaFiles: Array<MultipartFile>?,
        session: HttpSession,
        model: Model
    ): String {
        val userId = session.getAttribute("userId") as Long?

        if (userId == null) {
            return "redirect:/login"
        }

        if (!mediaService.isSupportedMediaFileList(mediaFiles)) {
            model.addAttribute("errorMessage", "mp3, mp4, pdf, docx, html 등 지원하는 학습자료 파일만 업로드할 수 있습니다.")
            model.addAttribute("title", title)
            model.addAttribute("content", content)
            model.addAttribute("category", category)
            return "post-write"
        }

        var createdPostId: Long? = null

        return try {
            val post = postService.createPost(userId, title, content, category)
            createdPostId = post.postId

            mediaService.saveUploadedMediaList(post.postId!!, mediaFiles)

            "redirect:/posts"
        } catch (e: Exception) {
            // 업로드 실패 시 빈 게시글이 남지 않도록 처리
            createdPostId?.let { postId ->
                runCatching { mediaService.deleteAllMediaByPostId(postId) }
                runCatching { postService.deletePost(postId, userId) }
            }

            model.addAttribute("errorMessage", "파일 업로드 중 오류가 발생했습니다. 파일명과 파일 형식을 확인해 주세요.")
            model.addAttribute("title", title)
            model.addAttribute("content", content)
            model.addAttribute("category", category)
            "post-write"
        }
    }

    @GetMapping("/{postId}")
    fun postDetail(
        @PathVariable postId: Long,
        model: Model,
        session: HttpSession
    ): String {
        val post = postService.getPost(postId)

        if (post == null) {
            return "redirect:/posts"
        }

        val mediaList = mediaService.getMediaListByPostId(postId)

        model.addAttribute("post", post)
        model.addAttribute("mediaList", mediaList)
        model.addAttribute("loginUserId", session.getAttribute("userId"))

        return "post-detail"
    }

    @GetMapping("/{postId}/edit")
    fun editPage(
        @PathVariable postId: Long,
        model: Model,
        session: HttpSession
    ): String {
        val userId = session.getAttribute("userId") as Long?

        if (userId == null) {
            return "redirect:/login"
        }

        val post = postService.getPost(postId)

        if (post == null || post.userId != userId) {
            return "redirect:/posts"
        }

        model.addAttribute("post", post)
        model.addAttribute("mediaList", mediaService.getMediaListByPostId(postId))
        return "post-edit"
    }

    @PostMapping("/{postId}/edit")
    fun editPost(
        @PathVariable postId: Long,
        @RequestParam title: String,
        @RequestParam content: String,
        @RequestParam category: String,
        @RequestParam("mediaFiles", required = false) mediaFiles: Array<MultipartFile>?,
        @RequestParam("deleteMediaIds", required = false) deleteMediaIds: List<Long>?,
        session: HttpSession,
        model: Model
    ): String {
        val userId = session.getAttribute("userId") as Long?

        if (userId == null) {
            return "redirect:/login"
        }

        val post = postService.getPost(postId)

        if (post == null || post.userId != userId) {
            return "redirect:/posts"
        }

        if (!mediaService.isSupportedMediaFileList(mediaFiles)) {
            model.addAttribute("errorMessage", "mp3, mp4, pdf, docx, html 등 지원하는 학습자료 파일만 업로드할 수 있습니다.")
            model.addAttribute("post", post)
            model.addAttribute("mediaList", mediaService.getMediaListByPostId(postId))
            return "post-edit"
        }

        return try {
            postService.updatePost(postId, userId, title, content, category)
            mediaService.deleteMediaList(postId, deleteMediaIds)
            mediaService.saveUploadedMediaList(postId, mediaFiles)

            "redirect:/posts/$postId"
        } catch (e: Exception) {
            model.addAttribute("errorMessage", "게시글 수정 중 파일 처리 오류가 발생했습니다. 파일명과 파일 형식을 확인해 주세요.")
            model.addAttribute("post", post)
            model.addAttribute("mediaList", mediaService.getMediaListByPostId(postId))
            "post-edit"
        }
    }

    @PostMapping("/{postId}/delete")
    fun deletePost(
        @PathVariable postId: Long,
        session: HttpSession
    ): String {
        val userId = session.getAttribute("userId") as Long?

        if (userId == null) {
            return "redirect:/login"
        }

        val post = postService.getPost(postId)

        if (post != null && post.userId == userId) {
            mediaService.deleteAllMediaByPostId(postId)
            postService.deletePost(postId, userId)
        }

        return "redirect:/posts"
    }
}
