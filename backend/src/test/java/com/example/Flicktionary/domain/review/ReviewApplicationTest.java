package com.example.Flicktionary.domain.review;

import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.review.dto.ReviewDto;
import com.example.Flicktionary.domain.review.repository.ReviewRepository;
import com.example.Flicktionary.domain.review.service.ReviewService;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.repository.UserAccountRepository;
import com.example.Flicktionary.global.dto.PageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("ReviewTest")
@Transactional
public class ReviewApplicationTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private SeriesRepository seriesRepository;

    private UserAccount testUser;
    private Movie testMovie;
    private Series testSeries;
    private ReviewDto reviewDto;

    @BeforeEach
    void beforeEach() {

        // 테스트용 User 엔티티 생성 및 저장
        testUser = userAccountRepository.save(new UserAccount(
                null, "테스트용 유저", "test12345", "test@email.com", "테스트 유저", null
                ));

        // 테스트용 Movie 엔티티 생성
        testMovie = movieRepository.save(Movie.builder()
                .tmdbId(10000000000L)
                .title("테스트용 영화 제목")
                .overview("테스트용 영화 줄거리")
                .releaseDate(LocalDate.of(2024, 1, 1))
                .posterPath("테스트용 이미지")
                .productionCountry("KR")
                .productionCompany("테스트용 제작사")
                .status("상영 중")
                .averageRating(4)
                .build()
        );

        // 테스트용 Series 엔티티 생성
        testSeries = seriesRepository.save(Series.builder()
                .title("테스트용 드라마 제목")
                .plot("테스트용 드라마 줄거리")
                .episode(12)
                .status("상영중")
                .imageUrl("테스트용 이미지")
                .avgRating(4.5)
                .ratingCount(10)
                .releaseStartDate(LocalDate.of(2024, 1, 1))
                .releaseEndDate(LocalDate.of(2200, 1, 2))
                .nation("KR")
                .company("테스트용 제작사")
                .build()
        );

        reviewDto = ReviewDto.builder()
                .userAccountId(testUser.getId())
                .nickname(testUser.getNickname())
                .movieId(testMovie.getId())
                .rating(5)
                .content("테스트용 리뷰 내용")
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
        assertThat(review.getMovieId()).isEqualTo(reviewDto.getMovieId());
        assertThat(review.getRating()).isEqualTo(reviewDto.getRating());
        assertThat(review.getContent()).isEqualTo(reviewDto.getContent());
    }

    @Test
    @DisplayName("리뷰 작성 실패(리뷰 내용이 null이거나 빈 문자열일 때 예외 발생)")
    void printReviewFailNull() {

        // 리뷰 내용이 비어있는 리뷰 생성
        ReviewDto reviewEmpty = ReviewDto.builder()
                .userAccountId(testUser.getId())
                .nickname(testUser.getNickname())
                .movieId(testMovie.getId())
                .rating(5)
                .content("")
                .build();

        // 리뷰 내용이 null인 리뷰 생성
        ReviewDto reviewNull = ReviewDto.builder()
                .userAccountId(testUser.getId())
                .nickname(testUser.getNickname())
                .movieId(testMovie.getId())
                .rating(5)
                .content(null)
                .build();

        /// 검증 ///
        assertThrows(IllegalArgumentException.class, () -> 
                reviewService.createReview(reviewEmpty), "리뷰 내용이 비어있으면 예외 발생");
        assertThrows(IllegalArgumentException.class, () -> 
                reviewService.createReview(reviewNull), "리뷰 내용이 null이면 예외 발생");
    }

    @Test
    @DisplayName("리뷰 작성 실패(평점이 없을 때 예외 발생)")
    void printReviewFailRating() {

        // 평점이 없는 리뷰 생성
        ReviewDto reviewNoRating = ReviewDto.builder()
                .userAccountId(testUser.getId())
                .nickname(testUser.getNickname())
                .movieId(testMovie.getId())
                .rating(0)
                .content("테스트용 리뷰 내용")
                .build();

        /// 검증 ///
        assertThrows(IllegalArgumentException.class, () -> 
                reviewService.createReview(reviewNoRating), "평점이 0이면 예외 발생");
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
    @DisplayName("특정 영화의 리뷰 조회")
    void printReviewByMovie() {

        // 리뷰 생성
        reviewService.createReview(reviewDto);

        // 영화 id로 영화를 찾아 리뷰들을 PageDto 변수에 저장
        PageDto<ReviewDto> reviewDtoPageDto = reviewService.reviewMovieDtoPage(reviewDto.getMovieId(), 0, 5);

        /// 검증 ///
        assertThat(reviewDtoPageDto).isNotNull();
        assertThat(reviewDtoPageDto.getItems()).isNotEmpty();
        assertThat(reviewDtoPageDto.getItems().get(0).getMovieId()).isEqualTo(reviewDto.getMovieId());
        assertThat(reviewDtoPageDto.getCurPageNo()).isEqualTo(1);
        assertThat(reviewDtoPageDto.getPageSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("특정 드라마의 리뷰 조회")
    void printReviewBySeries() {

        // 리뷰 생성
        reviewService.createReview(reviewDto);

        // 드라마 id로 드라마를 찾아 리뷰들을 PageDto 변수에 저장
        PageDto<ReviewDto> reviewDtoPageDto = reviewService.reviewSeriesDtoPage(reviewDto.getSeriesId(), 0, 5);

        /// 검증 ///
        assertThat(reviewDtoPageDto).isNotNull();
        assertThat(reviewDtoPageDto.getItems()).isNotEmpty();
        assertThat(reviewDtoPageDto.getItems().get(0).getSeriesId()).isEqualTo(reviewDto.getSeriesId());
        assertThat(reviewDtoPageDto.getCurPageNo()).isEqualTo(1);
        assertThat(reviewDtoPageDto.getPageSize()).isEqualTo(5);
    }

    @Test
    @DisplayName("리뷰 수정")
    void updateReview() {

        // 리뷰 생성
        ReviewDto savedReview = reviewService.createReview(reviewDto);

        // 수정할 리뷰 내용 변수에 저장
        ReviewDto updatedReviewDto = ReviewDto.builder()
                .id(savedReview.getId())
                .userAccountId(savedReview.getUserAccountId())
                .nickname(savedReview.getNickname())
                .movieId(savedReview.getMovieId())
                .rating(4)
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

    @Test
    @DisplayName("리뷰 페이징 처리 확인")
    void reviewPagingationTest() {

        // 리뷰 20개 생성 및 DB에 저장
        for (int i = 0; i < 20; i++) {
            ReviewDto savedReview = ReviewDto.builder()
                    .userAccountId(testUser.getId())
                    .nickname(testUser.getNickname())
                    .movieId(testMovie.getId())
                    .rating(4)
                    .content("테스트용 리뷰 내용 " + i)
                    .build();

            reviewService.createReview(savedReview);
        }
        
        // 페이지와 크기
        int page = 0, size = 5;

        // 페이징 적용해서 영화 리뷰 목록 조회
        PageDto<ReviewDto> reviewDtoPage = reviewService.reviewMovieDtoPage(testMovie.getId(), page, size);
        List<ReviewDto> reviews = reviewDtoPage.getItems();

        /// 검증 ///
        // 출력
        for (ReviewDto reviewDto : reviews) {
            System.out.println("리뷰 내용 : " + reviewDto.getContent());
        }

        assertThat(reviewDtoPage).isNotNull();
        assertThat(reviews).isNotEmpty();
        assertThat(reviewDtoPage.getPageSize()).isEqualTo(size);
        assertThat(reviewDtoPage.getCurPageNo()).isEqualTo(page + 1);
        assertThat(reviewDtoPage.getTotalItems()).isEqualTo(20);
        assertThat(reviewDtoPage.getTotalPages()).isEqualTo(4);
        assertThat(reviews.size()).isEqualTo(size);
    }
}
