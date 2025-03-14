package com.example.Flicktionary.domain.review.repository;

import com.example.Flicktionary.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 영화에 대한 리뷰를 페이징 처리해서 가져옴
    Page<Review> findByMovie_IdOrderByIdDesc(Long movieId, Pageable pageable);

    // 특정 드라마에 대한 리뷰를 페이징 처리해서 가져옴
    Page<Review> findBySeries_IdOrderByIdDesc(Long seriesId, Pageable pageable);

    // 닉네임 또는 리뷰 내용으로 검색하는 기능
    Page<Review> findByUserAccount_NicknameContainingOrContentContaining(String nickname, String content, Pageable pageable);

    // 중복 리뷰를 검사함
    boolean existsByUserAccount_IdAndMovie_IdAndSeries_Id(Long userAccountId, Long movieId, Long seriesId);

    // 특정 사용자가 특정 영화에 이미 리뷰를 작성했는지 확인
    boolean existsByUserAccount_IdAndMovie_Id(Long userAccountId, Long movieId);

    // 특정 사용자가 특정 드라마에 이미 리뷰를 작성했는지 확인
    boolean existsByUserAccount_IdAndSeries_Id(Long userAccountId, Long seriesId);

    // 모든 리뷰를 페이징하여 가져오는 기능 추가
    Page<Review> findAll(Pageable pageable);
}