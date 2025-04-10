package com.example.Flicktionary.domain.user.service

import com.example.Flicktionary.domain.review.service.ReviewService
import com.example.Flicktionary.domain.user.dto.UserAccountDto
import com.example.Flicktionary.domain.user.entity.UserAccount
import com.example.Flicktionary.domain.user.repository.UserAccountRepository
import com.example.Flicktionary.global.exception.ServiceException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.BDDAssertions.catchThrowable
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.then
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@DisplayName("회원 서비스 테스트")
@ExtendWith(MockitoExtension::class)
class UserAccountServiceTest {

    @Mock private lateinit var userAccountRepository: UserAccountRepository
    @Mock private lateinit var passwordEncoder: PasswordEncoder
    @Mock private lateinit var reviewService: ReviewService
    @InjectMocks private lateinit var userAccountService: UserAccountService

    private val userAccountDto = UserAccountDto(
        1L,
        "testUserDto",
        "testPasswordDto",
        "testDto@email.com",
        "testNicknameDto",
        "ADMIN"
    )

    @DisplayName("회원 정보가 담긴 DTO가 주어졌을때 회원을 가입시킨다.")
    @Test
    fun givenDtoWhenRegisteringUserWillPersistUserAccount() {
        given(userAccountRepository.save(any<UserAccount>()))
            .willReturn(userAccountDto.toEntity())
        given(passwordEncoder.encode(any<String>())).willReturn(userAccountDto.password)

        val resUserAccountDto = userAccountService.registerUser(userAccountDto)

        assertNotNull(resUserAccountDto)
        assertEquals(userAccountDto.id!!, resUserAccountDto.id)
        assertEquals(userAccountDto.username, resUserAccountDto.username)
        assertEquals(userAccountDto.password, resUserAccountDto.password)
        assertEquals(userAccountDto.email, resUserAccountDto.email)
        assertEquals(userAccountDto.nickname, resUserAccountDto.nickname)
        assertEquals(userAccountDto.role, resUserAccountDto.role)
        then(userAccountRepository).should().save(any<UserAccount>())
    }

    @DisplayName("존재하지 않는 회원의 정보를 수정할때 예외를 던진다.")
    @Test
    fun givenNonexistentUserIdWhenModifyingUserThenThrowsException() {
        // Mockito에서 확장 함수인 .findByIdOrNull()을 모킹하지 못하므로 Optional 사용
        given(userAccountRepository.findById(0L)).willReturn(Optional.empty())

        val thrown = catchThrowable {
            userAccountService.modifyUser(0L, userAccountDto)
        }

        assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
                .hasMessageContaining("0번 유저를 찾을 수 없습니다.")
    }

    @DisplayName("회원의 정보를 수정한다.")
    @Test
    fun givenUserIdWhenModifyingUserThenReturnsModifiedUserDto() {
        val newUserAccountDto = UserAccountDto(
            999L,
            "newusername",
            "newpassword",
            "newemail@email.com",
            "newnickname",
            "USER"
        )
        given(userAccountRepository.findById(userAccountDto.id!!))
            .willReturn(Optional.of(userAccountDto.toEntity()))
        given(userAccountRepository.save(any<UserAccount>()))
            .willReturn(newUserAccountDto.toEntity())
        given(passwordEncoder.encode(any<String>())).willReturn(newUserAccountDto.password)

        val result = userAccountService.modifyUser(userAccountDto.id!!, newUserAccountDto)

        assertNotNull(result)
        assertEquals(newUserAccountDto.id, result.id)
        assertEquals(newUserAccountDto.username, result.username)
        assertEquals(newUserAccountDto.password, result.password)
        assertEquals(newUserAccountDto.email, result.email)
        assertEquals(newUserAccountDto.nickname, result.nickname)
        assertEquals(newUserAccountDto.role, result.role)
        then(userAccountRepository).should().save(any<UserAccount>())
    }

    @DisplayName("고유 ID로 회원을 조회한다.")
    @Test
    fun givenUserIdWhenFindingUserThenReturnsUser() {
        given(userAccountRepository.findById(userAccountDto.id!!))
            .willReturn(Optional.of(userAccountDto.toEntity()))

        val result = userAccountService.getUserById(userAccountDto.id!!)

        assertNotNull(result)
        assertEquals(userAccountDto.id!!, result.id)
        assertEquals(userAccountDto.username, result.username)
        assertEquals(userAccountDto.password, result.password)
        assertEquals(userAccountDto.email, result.email)
        assertEquals(userAccountDto.nickname, result.nickname)
        assertEquals(userAccountDto.role, result.role)
    }

    @DisplayName("존재하지 않는 고유 ID로 회원을 조회할때 예외를 던진다.")
    @Test
    fun givenNonexistentUserIdWhenFindingUserThenThrowsException() {
        given(userAccountRepository.findById(999L)).willReturn(Optional.empty())

        val thrown = catchThrowable { userAccountService.getUserById(999L) }

        assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
                .hasMessage("999번 유저를 찾을 수 없습니다.")
    }

    @DisplayName("유저 ID로 회원을 조회한다.")
    @Test
    fun givenUserUsernameWhenFindingUserThenReturnsUser() {
        given(userAccountRepository.findByUsername(userAccountDto.username))
            .willReturn(userAccountDto.toEntity())

        val result = userAccountService.getUserByUsername(userAccountDto.username)

        assertNotNull(result)
        assertEquals(userAccountDto.id!!, result.id)
        assertEquals(userAccountDto.username, result.username)
        assertEquals(userAccountDto.password, result.password)
        assertEquals(userAccountDto.email, result.email)
        assertEquals(userAccountDto.nickname, result.nickname)
        assertEquals(userAccountDto.role, result.role.toString())
    }

    @DisplayName("존재하지 않는 유저 ID로 회원을 조회할때 예외를 던진다.")
    @Test
    fun givenNonexistentUserUsernameWhenFindingUserThenThrowsException() {
        given(userAccountRepository.findByUsername("nonexistentuser"))
            .willReturn(null)

        val thrown = catchThrowable { userAccountService.getUserByUsername("nonexistentuser") }

        assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
                .hasMessage("유저를 찾을 수 없습니다.")
    }

    @DisplayName("회원을 삭제한다.")
    @Test
    fun givenUserIdWhenDeletingUserThenReturnsDeletedUserUsername() {
        given(userAccountRepository.findById(userAccountDto.id!!))
            .willReturn(Optional.of(userAccountDto.toEntity()))

        val result = userAccountService.deleteUserById(userAccountDto.id!!)

        assertNotNull(result)
        assertEquals(userAccountDto.username, result)
        then(userAccountRepository).should().delete(any<UserAccount>())
    }

    @DisplayName("존재하지 않는 고유 ID로 회원을 삭제할때 예외를 던진다.")
    @Test
    fun givenNonexistentUserIdWhenDeletingUserThenThrowsException() {
        given(userAccountRepository.findById(999L)).willReturn(Optional.empty())

        val thrown = catchThrowable { userAccountService.deleteUserById(999L) }

        assertThat(thrown)
            .isInstanceOf(ServiceException::class.java)
                .hasMessage("999번 유저를 찾을 수 없습니다.")
    }

}