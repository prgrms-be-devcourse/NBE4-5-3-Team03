package com.example.Flicktionary.global.security

import com.example.Flicktionary.domain.user.service.UserAccountService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userAccountService: UserAccountService
): UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val userAccount = userAccountService.getUserByUsername(username)
        return CustomUserDetails(userAccount)
    }
}