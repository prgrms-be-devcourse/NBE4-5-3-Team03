package com.example.Flicktionary.global.security

import jakarta.servlet.http.HttpServletRequest
import lombok.NonNull
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
class CorsConfig(): CorsConfigurationSource {
    private val ALLOWED_ORIGIN = "http://localhost:3000"
    private val ALLOWED_METHODS = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    
    override fun getCorsConfiguration(@NonNull request: HttpServletRequest): CorsConfiguration {
        val config = CorsConfiguration()
        config.allowedOrigins = listOf(ALLOWED_ORIGIN)
        config.allowedMethods = ALLOWED_METHODS
        config.allowCredentials = true
        config.allowedHeaders = listOf("Authorization", "Content-Type", "Accept", "Origin")
        config.exposedHeaders = listOf("Authorization")
        config.maxAge = 3600L

        return config
    }
}