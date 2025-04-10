package com.example.Flicktionary.domain.review.repository

import com.example.Flicktionary.domain.review.entity.Review
import com.example.Flicktionary.domain.user.entity.UserAccount
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository : JpaRepository<Review, Long> {
    // 특정 영화에 대한 리뷰를 페이징 처리해서 가져옴
    fun findByMovie_IdOrderByIdDesc(movieId: Long, pageable: Pageable): Page<Review>

    // 특정 드라마에 대한 리뷰를 페이징 처리해서 가져옴
    fun findBySeries_IdOrderByIdDesc(seriesId: Long, pageable: Pageable): Page<Review>

    // 닉네임 또는 리뷰 내용으로 검색하는 기능
    fun findByUserAccount_NicknameContainingOrContentContaining(
        nickname: String,
        content: String,
        pageable: Pageable
    ): Page<Review>

    // 중복 리뷰를 검사함
    fun existsByUserAccount_IdAndMovie_IdAndSeries_Id(userAccountId: Long, movieId: Long, seriesId: Long): Boolean

    // 특정 사용자가 특정 영화에 이미 리뷰를 작성했는지 확인
    fun existsByUserAccount_IdAndMovie_Id(userAccountId: Long, movieId: Long): Boolean

    // 특정 사용자가 특정 드라마에 이미 리뷰를 작성했는지 확인
    fun existsByUserAccount_IdAndSeries_Id(userAccountId: Long, seriesId: Long): Boolean

    // 모든 리뷰를 페이징하여 가져오는 기능 추가
    override fun findAll(pageable: Pageable): Page<Review>

    // 유저 계정을 찾는 기능
    fun findByUserAccount(userAccount: UserAccount?): List<Review>
}