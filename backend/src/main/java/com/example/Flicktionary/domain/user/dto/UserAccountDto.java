package com.example.Flicktionary.domain.user.dto;

import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.entity.UserAccountType;

/**
 * {@link UserAccount}에 해당하는 DTO.
 *
 * @param id
 * @param username
 * @param password
 * @param email
 * @param nickname
 * @param role
 */
public record UserAccountDto(
        Long id,
        String username,
        String password,
        String email,
        String nickname,
        String role
) {
    public static UserAccountDto from(UserAccount userAccount) {
        return new UserAccountDto(
                userAccount.getId(),
                userAccount.getUsername(),
                userAccount.getPassword(),
                userAccount.getEmail(),
                userAccount.getNickname(),
                userAccount.getRole().toString()
        );
    }

    public UserAccount toEntity() {
        return new UserAccount(
                this.id,
                this.username,
                this.password,
                this.email,
                this.nickname,
                UserAccountType.valueOf(this.role)
        );
    }
}
