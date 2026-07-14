package com.studystream.config

import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.MultipartException

@ControllerAdvice
class UploadExceptionHandler {
    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSize(model: Model): String {
        // 업로드 용량 초과 화면 처리
        model.addAttribute("errorMessage", "파일 용량이 너무 큽니다. 500MB 이하의 파일을 업로드해 주세요.")
        return "post-write"
    }

    @ExceptionHandler(MultipartException::class)
    fun handleMultipartError(model: Model): String {
        // multipart 요청 오류 화면 처리
        model.addAttribute("errorMessage", "파일 업로드 요청을 처리하지 못했습니다. 다른 파일로 다시 시도해 주세요.")
        return "post-write"
    }
}
