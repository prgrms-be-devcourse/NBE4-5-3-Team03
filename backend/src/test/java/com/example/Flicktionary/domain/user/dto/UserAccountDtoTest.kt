package com.example.Flicktionary.domain.user.dto

import com.example.Flicktionary.domain.user.entity.UserAccount
import com.example.Flicktionary.domain.user.entity.UserAccountType
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("UserAccount DTO 테스트")
class UserAccountDtoTest {

    private val userAccount = UserAccount(
        1L,
        "testUser",
        "testPassword",
        "test@email.com",
        "testNickname",
        UserAccountType.ADMIN
    )

    private val userAccountDto = UserAccountDto(
        2L,
        "testUserDto",
        "testPasswordDto",
        "testDto@email.com",
        "testNicknameDto",
        "ADMIN"
    )


    @DisplayName("엔티티로부터 올바른 DTO를 생성한다.")
    @Test
    fun createDtoFromEntity() {
        val testDto = UserAccountDto.from(userAccount)
        assertNotNull(testDto)
        assertEquals(userAccount.id, testDto.id)
        assertEquals(userAccount.username, testDto.username)
        assertEquals(userAccount.password, testDto.password)
        assertEquals(userAccount.email, testDto.email)
        assertEquals(userAccount.nickname, testDto.nickname)
        assertEquals(userAccount.role.toString(), testDto.role)
    }

    @DisplayName("DTO로부터 올바른 엔티티를 생성한다.")
    @Test
    fun createEntityFromDto() {
        val testEntity = userAccountDto.toEntity()
        assertNotNull(testEntity)
        assertEquals(userAccountDto.id, testEntity.id)
        assertEquals(userAccountDto.username, testEntity.username)
        assertEquals(userAccountDto.password, testEntity.password)
        assertEquals(userAccountDto.email, testEntity.email)
        assertEquals(userAccountDto.nickname, testEntity.nickname)
        assertEquals(UserAccountType.valueOf(userAccountDto.role), testEntity.role)
        assertTrue(testEntity.favorites.isEmpty())
        assertTrue(testEntity.reviews.isEmpty())
    }
}