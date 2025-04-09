package com.example.Flicktionary.domain.post.entity

import com.example.Flicktionary.domain.user.entity.UserAccount
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "post")
class Post(
    // 게시글 id (기본키)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // 유저 id (외래키)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var userAccount: UserAccount? = null,

    // 게시글 제목
    @Column(length = 50, nullable = false)
    var title: String,

    // 게시글 내용
    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    // 게시글 생성 날짜
    var createdAt: LocalDateTime? = null,

    // 게시글 스포일러 여부 토글
    @Column(columnDefinition = "boolean default false")
    var isSpoiler: Boolean? = false
) {
    // PrePersist로 createdAt 시간 자동 갱신
    @PrePersist
    fun prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now()
        }
    }

    constructor() : this(null, null, "", "", null, false)
}
