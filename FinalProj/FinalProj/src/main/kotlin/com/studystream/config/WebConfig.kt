package com.studystream.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val uploadPath = Paths.get("uploads/media").toAbsolutePath().normalize().toUri().toString()
        val uploadLocation = if (uploadPath.endsWith("/")) uploadPath else "$uploadPath/"

        registry.addResourceHandler("/uploads/media/**")
            .addResourceLocations(uploadLocation)

        registry.addResourceHandler("/css/**")
            .addResourceLocations("classpath:/static/css/")
    }
}
