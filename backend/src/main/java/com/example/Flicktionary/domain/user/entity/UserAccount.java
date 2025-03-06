package com.example.Flicktionary.domain.user.entity;

import com.example.Flicktionary.domain.favorite.entity.Favorite;
import com.example.Flicktionary.domain.review.entity.Review;
import com.example.Flicktionary.global.utils.UuidUtils;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 유저를 나타내는 엔티티 클래스.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
// User는 흔히 쓰이는 이름이기 때문에, RDBMS단에서의 오류를 방지하기 위해 테이블 이름 지정
@Table(name = "user_tbl")
public class UserAccount {

    /**
     * 유저의 고유 ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 유저의 유저네임(로그인 ID).
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * 유저의 비밀번호.
     */
    @Column(nullable = false)
    private String password;

    /**
     * 유저의 이메일 주소.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * 유저의 닉네임.
     */
    @Column(nullable = false)
    private String nickname;

    /**
     * 유저의 리프레시 토큰.
     */
    @Column(length = 24)
    private String refreshToken;

    /**
     * 유저의 리프레시 토큰이 만료되는 시각.
     */
    private LocalDateTime refreshTokenExpiresAt;

    /**
     * 유저의 계정 분류.
     * 일반 유저에 해당하는 UserType.USER와 관리자에 해당하는 UserType.ADMIN이 있다.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserAccountType role = UserAccountType.USER;

    /**
     * 유저가 남긴 리뷰.
     */
    @OneToMany(mappedBy = "userAccount")
    private List<Review> reviews = new ArrayList<>();

    /**
     * 유저가 즐겨찾기에 등록한 작품.
     * 사용자가 삭제되면 Favorite도 같이 삭제됨.
     */
    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favorites = new ArrayList<>();

    public UserAccount(Long id, String username, String password, String email, String nickname, UserAccountType role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
    }
}
