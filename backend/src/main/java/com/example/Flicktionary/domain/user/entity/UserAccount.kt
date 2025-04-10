package com.example.Flicktionary.domain.user.entity

import com.example.Flicktionary.domain.favorite.entity.Favorite
import com.example.Flicktionary.domain.review.entity.Review
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 유저를 나타내는 엔티티 클래스.
 */
@Entity
class UserAccount(
    /**
     * 유저의 유저네임(로그인 ID).
     */
    @Column(nullable = false, unique = true)
    var username: String,

    /**
     * 유저의 비밀번호.
     */
    @Column(nullable = false)
    var password: String,

    /**
     * 유저의 이메일 주소.
     */
    @Column(nullable = false, unique = true)
    var email: String,

    /**
     * 유저의 닉네임.
     */
    @Column(nullable = false)
    var nickname: String,

    /**
     * 유저의 계정 분류.
     * 일반 유저에 해당하는 UserType.USER와 관리자에 해당하는 UserType.ADMIN이 있다.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserAccountType = UserAccountType.USER
) {
    /**
     * 유저의 고유 ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    constructor(id: Long?, username: String, password: String, email: String, nickname: String, role: UserAccountType): this(username, password, email, nickname, role) {
        this.id = id
    }

    /**
     * 유저의 리프레시 토큰.
     */
    @Column(length = 24)
    var refreshToken: String? = null

    /**
     * 유저의 리프레시 토큰이 만료되는 시각.
     */
    var refreshTokenExpiresAt: LocalDateTime? = null

    /**
     * 유저가 남긴 리뷰.
     */
    @OneToMany(mappedBy = "userAccount")
    var reviews: MutableList<Review> = mutableListOf()

    /**
     * 유저가 즐겨찾기에 등록한 작품.
     * 사용자가 삭제되면 Favorite도 같이 삭제됨.
     */
    @OneToMany(mappedBy = "userAccount", cascade = [CascadeType.ALL], orphanRemoval = true)
    var favorites: MutableList<Favorite> = mutableListOf()
}
