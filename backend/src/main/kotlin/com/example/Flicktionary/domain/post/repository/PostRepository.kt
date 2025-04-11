package com.example.Flicktionary.domain.post.repository

import com.example.Flicktionary.domain.post.entity.Post
import com.example.Flicktionary.domain.user.entity.UserAccount
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostRepository : JpaRepository<Post, Long> {
    // 제목으로 게시글 찾기
    fun findByTitleContaining(title: String, pageable: Pageable): Page<Post>

    // 내용으로 게시글 찾기
    fun findByContentContaining(content: String, pageable: Pageable): Page<Post>

    // 닉네임으로 게시글 찾기
    fun findByUserAccount_NicknameContaining(nickname: String, pageable: Pageable): Page<Post>

    // 유저 계정을 찾는 기능
    fun findByUserAccount(userAccount: UserAccount?): List<Post>
}
