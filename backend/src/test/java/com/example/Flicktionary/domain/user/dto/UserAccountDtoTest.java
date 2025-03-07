package com.example.Flicktionary.domain.user.dto;

import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.entity.UserAccountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserAccount DTO 테스트")
class UserAccountDtoTest {

    private final UserAccount userAccount = new UserAccount(
            1L,
            "testUser",
            "testPassword",
            "test@email.com",
            "testNickname",
            UserAccountType.ADMIN
    );

    private final UserAccountDto userAccountDto = new UserAccountDto(
            2L,
            "testUserDto",
            "testPasswordDto",
            "testDto@email.com",
            "testNicknameDto",
            "ADMIN"
    );

    @DisplayName("엔티티로부터 올바른 DTO를 생성한다.")
    @Test
    void createDtoFromEntity() {
        UserAccountDto testDto = UserAccountDto.from(userAccount);
        assertNotNull(testDto);
        assertEquals(userAccount.getId(), testDto.id());
        assertEquals(userAccount.getUsername(), testDto.username());
        assertEquals(userAccount.getPassword(), testDto.password());
        assertEquals(userAccount.getEmail(), testDto.email());
        assertEquals(userAccount.getNickname(), testDto.nickname());
        assertEquals(userAccount.getRole().toString(), testDto.role());
    }

    @DisplayName("DTO로부터 올바른 엔티티를 생성한다.")
    @Test
    void createEntityFromDto() {
        UserAccount testEntity = userAccountDto.toEntity();
        assertNotNull(testEntity);
        assertEquals(userAccountDto.id(), testEntity.getId());
        assertEquals(userAccountDto.username(), testEntity.getUsername());
        assertEquals(userAccountDto.password(), testEntity.getPassword());
        assertEquals(userAccountDto.email(), testEntity.getEmail());
        assertEquals(userAccountDto.nickname(), testEntity.getNickname());
        assertEquals(UserAccountType.valueOf(userAccountDto.role()), testEntity.getRole());
        assertTrue(testEntity.getFavorites().isEmpty());
        assertTrue(testEntity.getReviews().isEmpty());
    }
}