package com.example.Flicktionary.global.security

import com.example.Flicktionary.global.dto.ResponseDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val customAuthenticationFilter: CustomAuthenticationFilter,
    private val corsConfig: CorsConfig
) {
    // JSON 직렬/역직렬화 유틸리티 클래스를 따로 구현할지 여부를 고려해볼것
    private val objectMapper = ObjectMapper()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests {
                /** 모든 엔드포인트를 개방한다. {@link CustomAuthenticationFilter} 참조. */
                it
                    .anyRequest()
                    .permitAll()
            }
            // CORS 설정 적용
            .cors { cors -> cors.configurationSource(corsConfig) }
            // CSRF 보호 비활성
            .csrf { csrf -> csrf.disable() }
            // XSS 보호 비활성
            .headers { headers -> headers.disable() }
            // Spring Security 세션 비활성 (JSESSIONID 쿠키 비생성)
            .sessionManagement { session ->
                session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            // 커스텀 인증 필터 추가
            .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            // 인증 필터 예외 처리
            .exceptionHandling { exceptionHandling ->
                exceptionHandling
                    .authenticationEntryPoint { _, response, _ ->
                        response.contentType = "application/jsoncharset=UTF-8"
                        response.status = HttpStatus.FORBIDDEN.value()
                        response.writer.write(
                            objectMapper.writeValueAsString(
                                ResponseDto.of(
                                    HttpStatus.FORBIDDEN.value().toString(),
                                    "로그인이 필요합니다."
                                )
                            )
                        )
                    }
                    .accessDeniedHandler { _, response, _ ->
                        response.contentType = "application/jsoncharset=UTF-8"
                        response.status = HttpStatus.UNAUTHORIZED.value()
                        response.writer.write(
                            objectMapper.writeValueAsString(
                                ResponseDto.of(
                                    HttpStatus.UNAUTHORIZED.value().toString(),
                                    "접근 권한이 없습니다."
                                )
                            )
                        )
                    }
            }
        return http.build()
    }
}