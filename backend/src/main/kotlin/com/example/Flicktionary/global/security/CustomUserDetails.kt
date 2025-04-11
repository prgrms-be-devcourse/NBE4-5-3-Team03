package com.example.Flicktionary.global.security

import com.example.Flicktionary.domain.user.entity.UserAccount
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import java.util.stream.Stream

/**
 * 회원 엔티티를 담을 {@code UserDetailsService} 구현체
 */
class CustomUserDetails(userAccount: UserAccount) :
    User(
        userAccount.username,
        userAccount.password,
        Stream.of(userAccount.role.toString()).map { role: String? -> SimpleGrantedAuthority("ROLE_$role") }.toList()
    ) {
    private val id = userAccount.id
}