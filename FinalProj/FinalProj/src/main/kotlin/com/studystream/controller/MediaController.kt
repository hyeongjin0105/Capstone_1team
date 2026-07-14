package com.studystream.controller

import com.studystream.service.MediaService
import com.studystream.service.PostService
import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.nio.charset.StandardCharsets

@Controller
@RequestMapping("/media-play")
class MediaController(
    private val postService: PostService,
    private val mediaService: MediaService
) {
    @GetMapping("/{postId}")
    fun mediaPlayPage(
        @PathVariable postId: Long,
        model: Model
    ): String {
        val post = postService.getPost(postId)

        if (post == null) {
            return "redirect:/posts"
        }

        val mediaList = mediaService.getMediaListByPostId(postId)

        model.addAttribute("post", post)
        model.addAttribute("mediaList", mediaList)

        return "media-play"
    }

    @GetMapping("/download/{mediaId}")
    fun downloadFile(
        @PathVariable mediaId: Long
    ): ResponseEntity<Resource> {
        val media = mediaService.getMedia(mediaId)
            ?: return ResponseEntity.notFound().build()

        val resource = mediaService.loadFileResource(media)
        val contentDisposition = ContentDisposition.attachment()
            .filename(media.fileName, StandardCharsets.UTF_8)
            .build()
            .toString()

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
            .body(resource)
    }
}
