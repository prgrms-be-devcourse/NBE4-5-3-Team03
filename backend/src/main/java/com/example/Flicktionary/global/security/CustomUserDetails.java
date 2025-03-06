package com.example.Flicktionary.global.security;

import com.example.Flicktionary.domain.user.entity.UserAccount;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.stream.Stream;

/**
 * 회원 엔티티를 담을 {@code UserDetailsService} 구현체
 */
@Getter
public class CustomUserDetails extends User {

    private Long id;

    public CustomUserDetails(UserAccount userAccount) {
        super(userAccount.getUsername(), userAccount.getPassword(), Stream.of(userAccount.getRole().toString()).map(SimpleGrantedAuthority::new).toList());
        this.id = userAccount.getId();
    }
}
