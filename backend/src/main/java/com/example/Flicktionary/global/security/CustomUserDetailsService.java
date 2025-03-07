package com.example.Flicktionary.global.security;

import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountService userAccountService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount userAccount = userAccountService.getUserByUsername(username);
        return new CustomUserDetails(userAccount);
    }
}
