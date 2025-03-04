package com.example.Flicktionary;

import com.example.Flicktionary.domain.review.dto.ReviewDto;
import com.example.Flicktionary.domain.review.repository.ReviewRepository;
import com.example.Flicktionary.domain.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ReviewTest")
@Transactional
public class ReviewApplicationTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewService reviewService;

    private ReviewDto reviewDto;
    
    @BeforeEach
    void BeforeEach() {
        reviewDto = ReviewDto.builder()
                .user_id(1L)
                .movie_id(1L)
                .rating(5)
                .content("테스트 내용")
                .build();
    }

    @Test
    @DisplayName("출력 테스트")
    void printHelloWorld() {
        System.out.println("Hello World");
    }

    @Test
    @DisplayName("리뷰 작성")
    void printReview() {

        // 리뷰 생성 및 변수에 저장
        ReviewDto review = reviewService.createReview(reviewDto);

        /// 검증 ///
        assertThat(review).isNotNull();
        assertThat(review.getMovie_id()).isEqualTo(reviewDto.getMovie_id());
        assertThat(review.getRating()).isEqualTo(reviewDto.getRating());
        assertThat(review.getContent()).isEqualTo(reviewDto.getContent());
    }

    @Test
    @DisplayName("모든 리뷰 조회")
    void printAllReviews() {

        // 리뷰 생성
        reviewService.createReview(reviewDto);

        // 모든 리뷰 List 변수에 저장
        List<ReviewDto> reviews = reviewService.findAllReviews();

        /// 검증 ///
        assertThat(reviews).isNotEmpty();
    }

    @Test
    @DisplayName("리뷰 수정")
    void updateReview() {

        // 리뷰 생성
        ReviewDto savedReview = reviewService.createReview(reviewDto);

        // 수정할 리뷰 내용 변수에 저장
        ReviewDto updatedReviewDto = ReviewDto.builder()
                .id(savedReview.getId())
                .user_id(savedReview.getUser_id())
                .movie_id(savedReview.getMovie_id())
                .rating(3)
                .content("(테스트)수정된 리뷰 내용")
                .build();

        // 수정
        ReviewDto review = reviewService.updateReview(savedReview.getId(), updatedReviewDto);

        /// 검증 ///
        assertThat(review).isNotNull();
        assertThat(review.getRating()).isEqualTo(4);
        assertThat(review.getContent()).isEqualTo("(테스트)수정된 리뷰 내용");
    }

    @Test
    @DisplayName("리뷰 삭제")
    void deleteReview() {

        // 리뷰 생성
        ReviewDto savedReview = reviewService.createReview(reviewDto);

        // 리뷰 삭제
        reviewService.deleteReview(savedReview.getId());

        /// 검증 ///
        assertThat(reviewRepository.findById(savedReview.getId())).isEmpty();
    }
}
