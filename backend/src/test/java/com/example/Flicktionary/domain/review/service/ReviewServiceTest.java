package com.example.Flicktionary.domain.review.service;

import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.review.dto.ReviewDto;
import com.example.Flicktionary.domain.review.entity.Review;
import com.example.Flicktionary.domain.review.repository.ReviewRepository;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.entity.UserAccountType;
import com.example.Flicktionary.domain.user.repository.UserAccountRepository;
import com.example.Flicktionary.global.dto.PageDto;
import com.example.Flicktionary.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doNothing;

@DisplayName("리뷰 서비스 테스트")
@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private SeriesRepository seriesRepository;

    @InjectMocks
    private ReviewService reviewService;


    // 변수 설정
    private UserAccount testUser = new UserAccount(
            12L,
            "테스트용 유저",
            "test12345",
            "test@email.com",
            "테스트 유저",
            UserAccountType.USER);

    private Movie testMovie;

    private Series testSeries;

    private ReviewDto reviewDto1;

    private ReviewDto reviewDto2;

    @BeforeEach
    void initValues() {
        testMovie = new Movie(10000000000L, "테스트용 영화 제목", "테스트용 영화 줄거리",
                LocalDate.of(2024, 1, 1), "상영 중",
                "테스트용 이미지", 100, "KR", "테스트용 제작사");
        testMovie.setId(13L);
        testMovie.setAverageRating(4);
        testMovie.setRatingCount(10);

        testSeries = new Series(10000000000L, "테스트용 드라마 제목", "test series1",
                LocalDate.of(2024, 1, 1), LocalDate.of(2200, 1, 2),
                "상영중", "테스트용 이미지", 12, "KR", "테스트용 제작사");
        testSeries.setId(14L);
        testSeries.setAverageRating(4.5);
        testSeries.setRatingCount(10);

        reviewDto1 = ReviewDto.builder()
                .userAccountId(testUser.getId())
                .nickname(testUser.getNickname())
                .movieId(testMovie.getId())
                .rating(5)
                .content("테스트용 리뷰 내용 (영화)")
                .build();

        reviewDto2 = ReviewDto.builder()
                .userAccountId(testUser.getId())
                .nickname(testUser.getNickname())
                .seriesId(testSeries.getId())
                .rating(5)
                .content("테스트용 리뷰 내용 (드라마)")
                .build();
    }

    @Test
    @DisplayName("리뷰 작성")
    void printReview() {
        given(userAccountRepository.findById(reviewDto1.getUserAccountId())).willReturn(Optional.of(testUser));
        given(movieRepository.findById(reviewDto1.getMovieId())).willReturn(Optional.of(testMovie));
        given(reviewRepository.save(any(Review.class))).willReturn(Review.builder()
                .id(1L)
                .userAccount(testUser)
                .movie(testMovie)
                .rating(reviewDto1.getRating())
                .content(reviewDto1.getContent())
                .build());

        // 리뷰 생성 및 변수에 저장
        ReviewDto reviewDto = reviewService.createReview(reviewDto1);

        /// 검증 ///
        assertThat(reviewDto).isNotNull();
        assertThat(reviewDto.getMovieId()).isNotNull();
        assertThat(reviewDto.getSeriesId()).isNull();
        assertEquals(1L, reviewDto.getId());
        assertEquals(reviewDto1.getRating(), reviewDto.getRating());
        assertEquals(reviewDto1.getContent(), reviewDto.getContent());

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

        // userAccountRepository.findById()가 호출될 때 testUser를 반환하도록 스텁 처리
        given(userAccountRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
        // userAccountRepository.findById()가 호출될 때 testUser를 반환하도록 스텁 처리
        given(userAccountRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));

        Throwable thrownOnEmpty = catchThrowable(() -> reviewService.createReview(reviewEmpty));
        Throwable thrownOnNull = catchThrowable(() -> reviewService.createReview(reviewNull));

        /// 검증 ///
        assertThat(thrownOnEmpty)
                .isInstanceOf(ServiceException.class)
                .hasMessage("리뷰 내용을 입력해주세요.");
        assertThat(thrownOnNull)
                .isInstanceOf(ServiceException.class)
                .hasMessage("리뷰 내용을 입력해주세요.");
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

        // userAccountRepository.findById()가 호출될 때 testUser를 반환하도록 스텁 처리
        given(userAccountRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));

        Throwable thrown = catchThrowable(() -> reviewService.createReview(reviewNoRating));

        /// 검증 ///
        assertThat(thrown)
                .isInstanceOf(ServiceException.class)
                .hasMessage("평점을 매겨주세요.");
    }


    @Test
    @DisplayName("리뷰 작성 시 영화 정보 업데이트")
    void createReviewUpdateMovie() {
        int ratingCount = testMovie.getRatingCount();
        double averageRating = testMovie.getAverageRating();
        given(userAccountRepository.findById(any(Long.class))).willReturn(Optional.of(testUser));
        given(movieRepository.findById(any(Long.class))).willReturn(Optional.of(testMovie));
        given(reviewRepository.save(any(Review.class))).willReturn(reviewDto1.toEntity(testUser, testMovie, null));

        // 리뷰 생성 및 변수에 저장
        ReviewDto review = reviewService.createReview(reviewDto1);

        int newRatingCount = ratingCount + 1;
        double newAverageRating = (averageRating * ratingCount + review.getRating()) / newRatingCount;

        assertThat(testMovie.getRatingCount()).isEqualTo(newRatingCount);
        assertThat(testMovie.getAverageRating()).isEqualTo(newAverageRating);
        then(userAccountRepository).should().findById(reviewDto1.getUserAccountId());
        then(movieRepository).should().findById(reviewDto1.getMovieId());
        then(reviewRepository).should().save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 작성 시 시리즈 정보 업데이트")
    void createReviewUpdateSeries() {
        int ratingCount = testSeries.getRatingCount();
        double averageRating = testSeries.getAverageRating();
        given(userAccountRepository.findById(any(Long.class))).willReturn(Optional.of(testUser));
        given(seriesRepository.findById(any(Long.class))).willReturn(Optional.of(testSeries));
        given(reviewRepository.save(any(Review.class))).willReturn(reviewDto2.toEntity(testUser, null, testSeries));

        // 리뷰 생성 및 변수에 저장
        ReviewDto review = reviewService.createReview(reviewDto2);

        int newRatingCount = ratingCount + 1;
        double newAverageRating = (averageRating * ratingCount + review.getRating()) / newRatingCount;

        assertThat(testSeries.getRatingCount()).isEqualTo(newRatingCount);
        assertThat(testSeries.getAverageRating()).isEqualTo(newAverageRating);
        then(userAccountRepository).should().findById(reviewDto2.getUserAccountId());
        then(seriesRepository).should().findById(reviewDto2.getSeriesId());
        then(reviewRepository).should().save(any(Review.class));
    }

    @Test
    @DisplayName("모든 리뷰 조회")
    void printAllReviews() {

        // 변수 입력
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Page 객체 생성 리스트, pageable 객체, 전체 아이템 수를 넘김
        List<Review> reviewEntities = List.of(
                reviewDto1.toEntity(testUser, testMovie, null),
                reviewDto2.toEntity(testUser, null, testSeries)
        );
        Page<Review> reviewPage = new PageImpl<>(reviewEntities, pageable, reviewEntities.size());

        given(reviewRepository.findAll(pageable)).willReturn(reviewPage);

        // 반환 타입 변경
        PageDto<ReviewDto> reviewsPageDto = reviewService.findAllReviews(page, size);
        List<ReviewDto> reviews = reviewsPageDto.getItems();

        /// 검증 ///
        assertThat(reviews).isNotEmpty().hasSize(2);
        assertThat(reviews).flatExtracting(ReviewDto::getContent)
                .contains(reviewDto1.getContent(), reviewDto2.getContent());
        assertThat(reviews).flatExtracting(ReviewDto::getMovieId).contains(reviewDto1.getMovieId());
        assertThat(reviews).flatExtracting(ReviewDto::getSeriesId).contains(reviewDto2.getSeriesId());

        // PageDto에 대한 추가적인 검증 (예: totalItems, totalPages 등)이 필요할 수 있습니다.
        assertThat(reviewsPageDto.getTotalItems()).isEqualTo(2);
        assertThat(reviewsPageDto.getTotalPages()).isEqualTo(1); // 2개의 아이템, 페이지 사이즈 10이므로 1페이지

        then(reviewRepository).should().findAll(pageable);
    }

    @Test
    @DisplayName("특정 영화의 리뷰 조회")
    void printReviewByMovie() {
        given(reviewRepository.findByMovie_IdOrderByIdDesc(any(Long.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(
                        List.of(reviewDto1.toEntity(testUser, testMovie, null)),
                        PageRequest.of(0, 5),
                        1L)
                );

        // 영화 id로 영화를 찾아 리뷰들을 PageDto 변수에 저장
        PageDto<ReviewDto> reviewDtoPageDto = reviewService.reviewMovieDtoPage(reviewDto1.getMovieId(), 0, 5);

        /// 검증 ///
        assertThat(reviewDtoPageDto).isNotNull();
        assertThat(reviewDtoPageDto.getItems()).hasSize(1);
        assertThat(reviewDtoPageDto.getItems()).flatExtracting(ReviewDto::getMovieId)
                .contains(reviewDto1.getMovieId());
        assertEquals(1, reviewDtoPageDto.getCurPageNo());
        assertEquals(5, reviewDtoPageDto.getPageSize());
        then(reviewRepository).should().findByMovie_IdOrderByIdDesc(any(Long.class), any(Pageable.class));
    }

    @Test
    @DisplayName("특정 드라마의 리뷰 조회")
    void printReviewBySeries() {
        given(reviewRepository.findBySeries_IdOrderByIdDesc(any(Long.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(
                        List.of(reviewDto2.toEntity(testUser, null, testSeries)),
                        PageRequest.of(0, 5),
                        1L)
                );

        // 영화 id로 영화를 찾아 리뷰들을 PageDto 변수에 저장
        PageDto<ReviewDto> reviewDtoPageDto = reviewService.reviewSeriesDtoPage(reviewDto2.getSeriesId(), 0, 5);

        /// 검증 ///
        assertThat(reviewDtoPageDto).isNotNull();
        assertThat(reviewDtoPageDto.getItems()).hasSize(1);
        assertThat(reviewDtoPageDto.getItems()).flatExtracting(ReviewDto::getSeriesId)
                .contains(reviewDto2.getSeriesId());
        assertEquals(1, reviewDtoPageDto.getCurPageNo());
        assertEquals(5, reviewDtoPageDto.getPageSize());
        then(reviewRepository).should().findBySeries_IdOrderByIdDesc(any(Long.class), any(Pageable.class));
    }

    @DisplayName("리뷰 수정")
    @Test
        // TODO: 새로운 평점/내용 검증 로직까지 테스트하는 것을 검토
    void updateReview() {
        given(reviewRepository.findById(reviewDto1.getId()))
                .willReturn(Optional.of(reviewDto1.toEntity(testUser, testMovie, null)));

        // 수정할 리뷰 내용 변수에 저장
        ReviewDto updatedReviewDto = ReviewDto.builder()
                .id(reviewDto1.getId())
                .userAccountId(reviewDto1.getUserAccountId())
                .nickname(reviewDto1.getNickname())
                .movieId(reviewDto1.getMovieId())
                .rating(4)
                .content("(테스트)수정된 리뷰 내용")
                .build();

        // 수정
        ReviewDto result = reviewService.updateReview(reviewDto1.getId(), updatedReviewDto);

        /// 검증 ///
        assertThat(result).isNotNull();
        assertEquals(4, result.getRating());
        assertEquals("(테스트)수정된 리뷰 내용", result.getContent());
        then(reviewRepository).should().findById(reviewDto1.getId());
    }

    @Test
    @DisplayName("리뷰 수정 시 영화 정보 업데이트")
    void updateReviewUpdateMovie() {
        int ratingCount = testMovie.getRatingCount();
        double averageRating = testMovie.getAverageRating();
        // 수정할 리뷰 내용 변수에 저장
        ReviewDto updatedReviewDto = ReviewDto.builder()
                .id(reviewDto1.getId())
                .userAccountId(reviewDto1.getUserAccountId())
                .nickname(reviewDto1.getNickname())
                .movieId(reviewDto1.getMovieId())
                .rating(4)
                .content("(테스트)수정된 리뷰 내용")
                .build();
        given(reviewRepository.findById(reviewDto1.getId()))
                .willReturn(Optional.of(updatedReviewDto.toEntity(testUser, testMovie, null)));

        // 수정
        ReviewDto review = reviewService.updateReview(reviewDto1.getId(), updatedReviewDto);

        double newAverageRating = (averageRating * ratingCount - updatedReviewDto.getRating() + review.getRating()) / ratingCount;

        assertThat(testMovie.getRatingCount()).isEqualTo(ratingCount);
        assertThat(testMovie.getAverageRating()).isEqualTo(newAverageRating);
        then(reviewRepository).should().findById(reviewDto1.getId());
    }

    @Test
    @DisplayName("리뷰 수정 시 시리즈 정보 업데이트")
    void updateReviewUpdateSeries() {
        int ratingCount = testSeries.getRatingCount();
        double averageRating = testSeries.getAverageRating();
        // 수정할 리뷰 내용 변수에 저장
        ReviewDto updatedReviewDto = ReviewDto.builder()
                .id(reviewDto2.getId())
                .userAccountId(reviewDto2.getUserAccountId())
                .nickname(reviewDto2.getNickname())
                .movieId(reviewDto2.getMovieId())
                .rating(4)
                .content("(테스트)수정된 리뷰 내용")
                .build();
        given(reviewRepository.findById(reviewDto2.getId()))
                .willReturn(Optional.of(updatedReviewDto.toEntity(testUser, null, testSeries)));

        // 수정
        ReviewDto review = reviewService.updateReview(reviewDto2.getId(), updatedReviewDto);

        double newAverageRating = (averageRating * ratingCount - updatedReviewDto.getRating() + review.getRating()) / ratingCount;

        assertThat(testSeries.getRatingCount()).isEqualTo(ratingCount);
        assertThat(testSeries.getAverageRating()).isEqualTo(newAverageRating);
        then(reviewRepository).should().findById(reviewDto2.getId());
    }

    @DisplayName("존재하지 않는 리뷰 수정")
    @Test
    void updateNonexistentReview() {
        given(reviewRepository.findById(1234L)).willReturn(Optional.empty());


        // 수정
        Throwable thrown = catchThrowable(() -> reviewService.updateReview(1234L, reviewDto1));

        /// 검증 ///
        assertThat(thrown)
                .isInstanceOf(ServiceException.class)
                .hasMessage("1234번 리뷰를 찾을 수 없습니다.");
        then(reviewRepository).should().findById(1234L);
    }

    @DisplayName("리뷰 삭제")
    @Test
    void deleteReview() {
        given(reviewRepository.findById(reviewDto1.getId()))
                .willReturn(Optional.of(reviewDto1.toEntity(testUser, testMovie, null)));

        // 리뷰 삭제
        reviewService.deleteReview(reviewDto1.getId());

        /// 검증 ///
        then(reviewRepository).should().findById(reviewDto1.getId());
        then(reviewRepository).should().delete(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 삭제 시 영화 정보 업데이트")
    void deleteReviewUpdateMovie() {
        int ratingCount = testMovie.getRatingCount();
        double averageRating = testMovie.getAverageRating();
        given(reviewRepository.findById(reviewDto1.getId()))
                .willReturn(Optional.of(reviewDto1.toEntity(testUser, testMovie, null)));
        doNothing().when(reviewRepository).delete(any(Review.class));

        // 리뷰 삭제
        reviewService.deleteReview(reviewDto1.getId());

        int newRatingCount = ratingCount - 1;
        double newAverageRating = (averageRating * ratingCount - reviewDto1.getRating()) / newRatingCount;

        assertThat(testMovie.getRatingCount()).isEqualTo(newRatingCount);
        assertThat(testMovie.getAverageRating()).isEqualTo(newAverageRating);
        then(reviewRepository).should().findById(reviewDto1.getId());
        then(reviewRepository).should().delete(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 삭제 시 시리즈 정보 업데이트")
    void deleteReviewUpdateSeries() {
        int ratingCount = testSeries.getRatingCount();
        double averageRating = testSeries.getAverageRating();
        given(reviewRepository.findById(reviewDto2.getId()))
                .willReturn(Optional.of(reviewDto2.toEntity(testUser, null, testSeries)));
        doNothing().when(reviewRepository).delete(any(Review.class));

        // 리뷰 삭제
        reviewService.deleteReview(reviewDto2.getId());

        int newRatingCount = ratingCount - 1;
        double newAverageRating = (averageRating * ratingCount - reviewDto2.getRating()) / newRatingCount;

        assertThat(testSeries.getRatingCount()).isEqualTo(newRatingCount);
        assertThat(testSeries.getAverageRating()).isEqualTo(newAverageRating);
        then(reviewRepository).should().findById(reviewDto2.getId());
        then(reviewRepository).should().delete(any(Review.class));
    }

    @DisplayName("존재하지 않는 리뷰 삭제")
    @Test
    void deleteNonexistentReview() {
        given(reviewRepository.findById(reviewDto1.getId())).willReturn(Optional.empty());

        // 리뷰 삭제
        Throwable thrown = catchThrowable(() -> reviewService.deleteReview(reviewDto1.getId()));

        /// 검증 ///
        assertThat(thrown)
                .isInstanceOf(ServiceException.class)
                .hasMessage("%d번 리뷰를 찾을 수 없습니다.".formatted(reviewDto1.getId()));
    }
}
