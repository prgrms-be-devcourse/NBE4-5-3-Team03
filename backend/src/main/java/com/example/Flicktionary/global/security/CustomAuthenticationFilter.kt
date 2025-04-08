package com.example.Flicktionary.global.security

import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService
import com.example.Flicktionary.global.dto.ResponseDto
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import java.net.URI

/**
 * 쿠키로 JWT를 받아 유저를 인증하는 커스텀 인증 필터.
 * OncePerRequestFilter을 상속하므로, {@link SecurityConfig}의 엔드포인트 보호 설정을 무시한다.
 */
@Component
class CustomAuthenticationFilter(
    private val userAccountJwtAuthenticationService: UserAccountJwtAuthenticationService,
    private val customUserDetailsService: CustomUserDetailsService
): OncePerRequestFilter() {

    /**
     * URI 경로를 검증하는 AntPathMatcher 오브젝트.
     */
    private val pathMatcher = AntPathMatcher()

    /**
     * 인증에서 제회할 URI 경로.
     */
    private val excludedUrls = arrayOf(
        "/api/users/login",
        "/api/users/register",
        "/api/users/status",
        "/api/users/refresh",
        "/h2-console/**"
    )

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val url = request.requestURI
        return excludedUrls.any { pathMatcher.match(it, url) }
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val cookies = request.cookies
        // 쿠키가 비어있다면 인증을 거부한다.
        if (cookies == null) {
            filterChain.doFilter(request, response)
            return
        }

        // 접근 토큰이 없거나 빈 값을 가지고 있다면 인증을 거부한다.
        val authCookie = cookies.find { it.name.equals("accessToken") }
        if (authCookie == null || authCookie.value.isEmpty()) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            // 접근 토큰에서 회원 정보를 꺼내 해당 회원을 현재 인증된 회원으로 지정한다.
            val requestUser = userAccountJwtAuthenticationService.retrieveUserFromAccessToken(authCookie.value)
            val customUserDetails = customUserDetailsService.loadUserByUsername(requestUser.username)
            val authentication = UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.authorities)
            SecurityContextHolder.getContext().authentication = authentication
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            // 접근 토큰이 유효하지 않을때 리프레시 토큰으로 재발급을 시도한다.
            response.contentType = "application/jsoncharset=UTF-8"
            response.status = HttpStatus.TEMPORARY_REDIRECT.value()
            response.setHeader("Location", URI.create("/api/users/refresh").toString())
            response.writer.write(
                ObjectMapper().writeValueAsString(
                    ResponseDto.of(
                    HttpStatus.TEMPORARY_REDIRECT.toString(),
                    "접근 토큰이 유효하지 않습니다."
                )))
        }
    }
}