package com.studystream.service

import com.studystream.domain.Media
import com.studystream.repository.MediaRepository
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@Service
class MediaService(
    private val mediaRepository: MediaRepository
) {
    private val uploadDir: Path = Paths.get("uploads/media").toAbsolutePath().normalize()

    fun saveUploadedMedia(postId: Long, mediaFile: MultipartFile): Media? {
        if (mediaFile.isEmpty) {
            return null
        }

        val mediaType = getMediaType(mediaFile)
            ?: throw IllegalArgumentException("지원하지 않는 파일 형식입니다.")

        Files.createDirectories(uploadDir)

        val originalFileName = mediaFile.originalFilename ?: "upload-file"
        val safeFileName = makeSafeFileName(originalFileName)
        val savedFileName = "${UUID.randomUUID()}_$safeFileName"
        val targetPath = uploadDir.resolve(savedFileName).normalize()

        if (!targetPath.startsWith(uploadDir)) {
            throw IllegalArgumentException("잘못된 파일 경로입니다.")
        }

        mediaFile.inputStream.use { input ->
            Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
        }

        val media = Media(
            postId = postId,
            mediaType = mediaType,
            fileName = safeFileName,
            filePath = "/uploads/media/$savedFileName",
            contentType = getContentType(mediaFile, mediaType)
        )

        return mediaRepository.save(media)
    }

    fun saveUploadedMediaList(postId: Long, mediaFiles: Array<MultipartFile>?): List<Media> {
        val uploadFiles = mediaFiles.orEmpty().filter { !it.isEmpty }
        return uploadFiles.mapNotNull { mediaFile -> saveUploadedMedia(postId, mediaFile) }
    }

    fun isSupportedMediaFile(mediaFile: MultipartFile?): Boolean {
        if (mediaFile == null || mediaFile.isEmpty) {
            return true
        }

        return getMediaType(mediaFile) != null
    }

    fun isSupportedMediaFileList(mediaFiles: Array<MultipartFile>?): Boolean {
        return mediaFiles.orEmpty()
            .filter { !it.isEmpty }
            .all { mediaFile -> isSupportedMediaFile(mediaFile) }
    }

    fun getMediaListByPostId(postId: Long): List<Media> {
        return mediaRepository.findByPostId(postId)
    }

    fun getMedia(mediaId: Long): Media? {
        return mediaRepository.findById(mediaId).orElse(null)
    }

    @Transactional
    fun deleteMedia(postId: Long, mediaId: Long): Boolean {
        val media = mediaRepository.findByMediaIdAndPostId(mediaId, postId) ?: return false

        deletePhysicalFile(media)
        mediaRepository.delete(media)
        return true
    }

    @Transactional
    fun deleteMediaList(postId: Long, mediaIds: List<Long>?) {
        mediaIds.orEmpty().forEach { mediaId ->
            deleteMedia(postId, mediaId)
        }
    }

    @Transactional
    fun deleteAllMediaByPostId(postId: Long) {
        val mediaList = mediaRepository.findByPostId(postId)

        mediaList.forEach { media ->
            deletePhysicalFile(media)
        }

        mediaRepository.deleteByPostId(postId)
    }

    fun loadFileResource(media: Media): Resource {
        val savedFileName = media.filePath.substringAfterLast("/")
        val filePath = uploadDir.resolve(savedFileName).normalize()

        // 업로드 폴더 밖 파일 접근 방지
        if (!filePath.startsWith(uploadDir) || !Files.exists(filePath)) {
            throw IllegalArgumentException("파일을 찾을 수 없습니다.")
        }

        val resource = UrlResource(filePath.toUri())

        if (!resource.exists() || !resource.isReadable) {
            throw IllegalArgumentException("파일을 읽을 수 없습니다.")
        }

        return resource
    }

    fun getMediaContentAsText(mediaId: Long): String {
        val media = getMedia(mediaId)
            ?: throw IllegalArgumentException("존재하지 않는 미디어 ID입니다.")

        return when (media.contentType) {
            "text/plain", "text/markdown", "text/html" -> {
                val resource = loadFileResource(media)
                resource.inputStream.use { it.reader().readText() }
            }
            "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                throw IllegalArgumentException("PDF, DOCX 파일의 텍스트 추출은 아직 지원되지 않습니다.")
            }
            else -> {
                throw IllegalArgumentException("텍스트를 추출할 수 있는 파일 형식이 아닙니다 (예: txt, md, html).")
            }
        }
    }

    private fun deletePhysicalFile(media: Media) {
        val savedFileName = media.filePath.substringAfterLast("/")

        if (savedFileName.isBlank()) {
            return
        }


        val filePath = uploadDir.resolve(savedFileName).normalize()

        // 업로드 폴더 밖 파일 삭제 방지
        if (filePath.startsWith(uploadDir) && Files.isRegularFile(filePath)) {
            Files.deleteIfExists(filePath)
        }
    }

    private fun getMediaType(mediaFile: MultipartFile): String? {
        val contentType = mediaFile.contentType?.lowercase() ?: ""
        val extension = getExtension(mediaFile.originalFilename)

        // 브라우저가 MIME 타입을 정상 전달한 경우
        if (contentType.startsWith("audio/")) {
            return "audio"
        }

        if (contentType.startsWith("video/")) {
            return "video"
        }

        if (contentType == "application/pdf") {
            return "document"
        }

        if (contentType == "text/html") {
            return "html"
        }

        // MIME 타입이 비어있거나 application/octet-stream으로 오는 경우 확장자로 확인
        return when (extension) {
            "mp3", "wav", "ogg", "m4a", "aac", "flac" -> "audio"
            "mp4", "mov", "avi", "mkv", "webm" -> "video"
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "md" -> "document"
            "html", "htm" -> "html"
            else -> null
        }
    }

    private fun getContentType(mediaFile: MultipartFile, mediaType: String): String {
        val contentType = mediaFile.contentType ?: ""
        val extension = getExtension(mediaFile.originalFilename)

        if (contentType.startsWith("audio/") || contentType.startsWith("video/")) {
            return contentType
        }

        return when (extension) {
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "flac" -> "audio/flac"
            "mp4" -> "video/mp4"
            "mov" -> "video/quicktime"
            "avi" -> "video/x-msvideo"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "txt" -> "text/plain"
            "md" -> "text/markdown"
            "html", "htm" -> "text/html"
            else -> if (mediaType == "audio") "audio/mpeg" else if (mediaType == "video") "video/mp4" else "application/octet-stream"
        }
    }

    private fun makeSafeFileName(originalFileName: String): String {
        val fileNameOnly = Paths.get(originalFileName).fileName.toString()
        val replacedName = fileNameOnly
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .replace(Regex("\\s+"), "_")
            .trim('_')

        val safeName = if (replacedName.isBlank()) "upload-file" else replacedName
        val extension = getExtension(safeName)
        val baseName = if (extension.isBlank()) safeName else safeName.substringBeforeLast(".")

        // DB 컬럼 길이 초과 방지
        val shortBaseName = baseName.take(70)

        return if (extension.isBlank()) {
            shortBaseName
        } else {
            "$shortBaseName.$extension"
        }
    }

    private fun getExtension(fileName: String?): String {
        if (fileName.isNullOrBlank() || !fileName.contains(".")) {
            return ""
        }

        return fileName.substringAfterLast(".").lowercase()
    }
}
