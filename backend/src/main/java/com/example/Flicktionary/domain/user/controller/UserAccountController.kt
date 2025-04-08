package com.example.Flicktionary.domain.user.controller

import com.example.Flicktionary.domain.user.dto.UserAccountDto
import com.example.Flicktionary.domain.user.service.UserAccountService
import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService
import com.example.Flicktionary.global.dto.ResponseDto
import com.example.Flicktionary.global.security.CustomUserDetails
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*


/**
 * 회원 도메인에 해당하는 API 엔드포인트
 */
@RequestMapping("/api/users")
@RestController
class UserAccountController(
    private val userAccountService: UserAccountService,
    private val userAccountJwtAuthenticationService: UserAccountJwtAuthenticationService
) {

    /**
     * 회원 가입
     *
     * @param userAccountDto 요청 본문에서 파싱한 회원 정보 DTO
     * @return 영속된 회원 엔티티에 해당하는 DTO
     */
    @PostMapping("/register")
    fun createUser(@RequestBody userAccountDto: UserAccountDto): ResponseEntity<ResponseDto<UserAccountDto>> {
        return ResponseEntity.ok(ResponseDto.ok(userAccountService.registerUser(userAccountDto)))
    }

    /**
     * 로그인하고자 하는 회원의 인증정보를 담는 DTO
     *
     * @param username 회원의 유저 ID
     * @param password 회원의 비밀번호
     */
    data class LoginRequest(
        val username: String,
        val password: String
    )

    /**
     * 회원 로그인. 성공시 접근 토큰과 리프레시 토큰을 새로 생성한 뒤 쿠키로 반환한다.
     *
     * @param loginRequest 로그인하고자 하는 회원의 인증정보
     * @param response
     * @return 성공 메시지 문자열을 답고 있는 ResponseEntity 오브젝트
     */
    @PostMapping("/login")
    fun loginUser(@RequestBody loginRequest: LoginRequest, response: HttpServletResponse): ResponseEntity<ResponseDto<*>> {
        // DB 접근을 1회 줄이기 위해 아래 UserAccountJwtAuthenticationService의 두 메서드를 하나로 합치는 것을 검토
        val accessToken = newCookieWithDefaultSettings("accessToken", userAccountJwtAuthenticationService.createNewAccessTokenForUser(loginRequest.username, loginRequest.password))
        val refreshToken = newCookieWithDefaultSettings("refreshToken", userAccountJwtAuthenticationService.rotateRefreshTokenOfUser(loginRequest.username))
        response.addCookie(accessToken)
        response.addCookie(refreshToken)

        return ResponseEntity.ok(ResponseDto.ok("토큰이 성공적으로 발행되었습니다."))
    }

    /**
     * 회원 로그아웃. 접근 토큰과 리프레시 토큰을 담고 있는 쿠키에 빈 문자열을 할당한다.
     *
     * @param response
     * @return 성공 메시지 문자열을 담고 있는 ResponseEntity 오브젝트
     */
    @GetMapping("/logout")
    fun logoutUser(response: HttpServletResponse): ResponseEntity<ResponseDto<*>> {
        val accessToken = newCookieWithDefaultSettings("accessToken", "")
        val refreshToken = newCookieWithDefaultSettings("refreshToken", "")
        response.addCookie(accessToken)
        response.addCookie(refreshToken)

        return ResponseEntity.ok(ResponseDto.ok("쿠키가 성공적으로 비워졌습니다."))
    }

    /**
     * 접근 토큰 재생성. 쿠키로 리프레시 토큰을 받은 뒤, 검증이 통과하면 새 접근 토큰과 리프레시 토큰을 발행하여 쿠키로 반환한다.
     *
     * @param refreshTokenBase64 base64로 인코딩된 리프레시 토큰 문자열
     * @param response
     * @return 성공 메시지 문자열을 담고 있는 ResponseEntity 오브젝트
     */
    @GetMapping("/refresh")
    fun refreshAccessToken(@CookieValue("refreshToken") refreshTokenBase64: String, response: HttpServletResponse): ResponseEntity<ResponseDto<*>> {
        val tokenSet = userAccountJwtAuthenticationService.createNewAccessTokenWithRefreshToken(refreshTokenBase64)
        val accessToken = newCookieWithDefaultSettings("accessToken", tokenSet.access())
        val refreshToken = newCookieWithDefaultSettings("refreshToken", tokenSet.refresh())
        response.addCookie(accessToken)
        response.addCookie(refreshToken)

        return ResponseEntity.ok(ResponseDto.ok("토큰이 성공적으로 재발행되었습니다."))
    }

    /**
     * 인증 정보를 HttpOnly 쿠키로 저장하기 때문에, 클라이언트는 현재 로그인 상태를 알 수 없다. 이를 해결하기 위해 서버에 요청을 보내
     * 인증 쿠키의 유무에 따른 응답을 반환한다.
     *
     * @param accessToken  접근 토큰 문자열
     * @param refreshToken 리프레시 토큰 문자열
     * @return ResponseEntity 오브젝트
     */
    @GetMapping("/status")
    fun verifyUserHasCredentials(
        @CookieValue(value = "accessToken", defaultValue = "") accessToken: String,
        @CookieValue(value = "refreshToken", defaultValue = "") refreshToken: String
    ): ResponseEntity<ResponseDto<*>> {
        if (accessToken.isEmpty() || refreshToken.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ResponseDto.of(
                    HttpStatus.FORBIDDEN.value().toString(),
                    "인증 정보가 존재하지 않습니다."
                )
                )
        }
        return ResponseEntity.ok(ResponseDto.ok("인증 정보가 존재합니다."))
    }

    /**
     * 회원정보 수정. 수정할 회원의 고유 ID와 새 회원정보를 받아, 기존 회원의 가입정보를 덮어쓴다.
     *
     * @param id             수정할 회원의 고유 ID
     * @param userAccountDto 새로 덮어씌워질 가입정보
     * @return 수정된 회원에 해당하는 DTO
     */
    @PutMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody userAccountDto: UserAccountDto): ResponseEntity<ResponseDto<UserAccountDto>> {
        return ResponseEntity.ok(ResponseDto.ok(userAccountService.modifyUser(id, userAccountDto)))
    }

    /**
     * 회원 닉네임 수정. 특정 회원의 닉네임만 변경할 수 있다.
     *
     * @param id             수정할 회원의 고유 ID
     * @param userAccountDto 새로운 닉네임 정보를 담은 DTO
     * @return 수정된 회원의 닉네임 정보를 포함한 DTO
     */

    @PatchMapping("/{id}/nickname")
    fun updateNickname(@PathVariable id: Long, @RequestBody userAccountDto: UserAccountDto): ResponseEntity<ResponseDto<UserAccountDto>> {
        return ResponseEntity.ok(ResponseDto.ok(userAccountService.modifyNickname(id, userAccountDto)))
    }

    /**
     * 회원 비밀번호 수정. 특정 회원의 비밀번호만 변경할 수 있다.
     *
     * @param id             수정할 회원의 고유 ID
     * @param userAccountDto 새로운 비밀번호 정보를 담은 DTO
     * @return 수정된 회원의 정보를 포함한 DTO
     */
    @PatchMapping("/{id}/password")
    fun updatePassword(@PathVariable id: Long, @RequestBody userAccountDto: UserAccountDto): ResponseEntity<ResponseDto<UserAccountDto>> {
        return ResponseEntity.ok(ResponseDto.ok(userAccountService.modifyPassword(id, userAccountDto)))
    }

    /**
     * 회원정보 조회. 조회할 회원의 고유 ID를 받아 해당 회원의 정보를 반환한다.
     *
     * @param id 조회할 회원의 고유 ID
     * @return 조회된 회원에 해당하는 DTO
     */
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<ResponseDto<UserAccountDto>> {
        return ResponseEntity.ok(ResponseDto.ok(userAccountService.getUserById(id)))
    }

    /**
     * 회원정보 조회. 현재 인증되어있는 회원의 정보를 반환한다.
     *
     * @param principal 현재 인증되어 있는 회원의 정보
     * @return 조회된 회원에 해당하는 DTO
     */
    @GetMapping
    fun getUserByPrinciple(@AuthenticationPrincipal principal: CustomUserDetails): ResponseEntity<ResponseDto<UserAccountDto>> {
        return ResponseEntity.ok(ResponseDto.ok(UserAccountDto.from(userAccountService.getUserByUsername(principal.username))))
    }

    /**
     * 회원 탈퇴. 탈퇴시킬 회원의 고유 ID를 받아 해당 회원을 탈퇴시킨다.
     *
     * @param id 탈퇴할 회원의 고유 ID
     * @return 탈퇴된 회원의 유저 ID
     */
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<ResponseDto<*>> {
        return ResponseEntity.ok(ResponseDto.ok(userAccountService.deleteUserById(id)))
    }

    /**
     * 이름과 값을 지정해 새 쿠키 오브젝트를 생성한다.
     *
     * @param name  쿠키의 이름
     * @param value 쿠키의 값
     * @return 새 쿠키 오브젝트
     */
    private fun newCookieWithDefaultSettings(name: String, value: String): Cookie {
        val cookie = Cookie(name, if (value == null) "" else value)
        cookie.isHttpOnly = true
        cookie.domain = "localhost"
        cookie.path = "/"
        // 300일 후 쿠키 만료
        cookie.maxAge = 25920000
        cookie.setAttribute("SameSite", "Strict")
        return cookie
    }
}