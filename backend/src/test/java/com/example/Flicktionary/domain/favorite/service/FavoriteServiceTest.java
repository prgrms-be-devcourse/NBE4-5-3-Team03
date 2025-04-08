package com.example.Flicktionary.domain.favorite.service;

import com.example.Flicktionary.domain.favorite.dto.FavoriteDto;
import com.example.Flicktionary.domain.favorite.entity.ContentType;
import com.example.Flicktionary.domain.favorite.entity.Favorite;
import com.example.Flicktionary.domain.favorite.repository.FavoriteRepository;
import com.example.Flicktionary.domain.movie.entity.Movie;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("즐겨찾기 서비스 테스트")
@ExtendWith(MockitoExtension.class)
public class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private SeriesRepository seriesRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    private UserAccount testUser = new UserAccount(
            123L,
            "tester1234",
            "tester1234",
            "tester1234@email.com",
            "tester1234",
            UserAccountType.USER
            );


    private Movie testMovie1;

    private Movie testMovie2;

    private Series testSeries;

    private FavoriteDto favorite1;

    private FavoriteDto favorite2;

    private Long movieCount = 2L;
    private Long seriesCount = 1L;

    @BeforeEach
    void setUp() {
        testMovie1 = new Movie(9599L, "test movie1", "test movie1",
                LocalDate.of(2024, 1, 1), "test movie1", "test movie1",
                100, "KR", "test movie1");
        testMovie1.setId(95L);
        testMovie1.setAverageRating(4);
        testMovie1.setRatingCount(15);

        testMovie2 = new Movie(599L, "test movie2", "test movie2",
                LocalDate.of(2024, 1, 1), "test movie2", "test movie2",
                100, "KR", "test movie2");
        testMovie2.setId(59L);
        testMovie2.setAverageRating(3.2);
        testMovie2.setRatingCount(151);

        testSeries = new Series(9519L, "test series1", "test series1",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1),
                "test series1", "test series1", 1, "KR", "test series1");
        testSeries.setId(101L);
        testSeries.setAverageRating(4.3);
        testSeries.setRatingCount(5);

        favorite1 = FavoriteDto.builder()
                .userId(testUser.getId())
                .contentType(ContentType.MOVIE)
                .contentId(testMovie1.getId())
                .build();

        favorite2 = FavoriteDto.builder()
                .userId(testUser.getId())
                .contentType(ContentType.SERIES)
                .contentId(testSeries.getId())  // 가상의 Series ID
                .build();
    }

    @Test
    @DisplayName("즐겨찾기 추가 성공")
    void createFavorite_Success() {
        ArgumentCaptor<Favorite> captor = ArgumentCaptor.forClass(Favorite.class);
        given(userAccountRepository.findById(testUser.getId()))
                .willReturn(Optional.of(testUser));
        given(favoriteRepository.existsByUserAccountIdAndContentTypeAndContentId(
                favorite1.getUserId(),
                favorite1.getContentType(),
                favorite1.getContentId()))
                .willReturn(false);
        given(movieRepository.existsById(favorite1.getContentId())).willReturn(true);
        given(favoriteRepository.save(captor.capture())).willReturn(favorite1.toEntity(testUser));

        FavoriteDto result = favoriteService.createFavorite(favorite1);
        Favorite captured = captor.getValue();

        assertThat(result).isNotNull();
        assertEquals(favorite1.getUserId(), captured.getUserAccount().getId());
        assertEquals(favorite1.getContentType(), captured.getContentType());
        assertEquals(favorite1.getContentId(), captured.getContentId());
        assertEquals(favorite1.getUserId(), result.getUserId());
        assertEquals(favorite1.getContentType(), result.getContentType());
        assertEquals(favorite1.getContentId(), result.getContentId());
        then(userAccountRepository).should().findById(testUser.getId());
        then(favoriteRepository).should().existsByUserAccountIdAndContentTypeAndContentId(
                favorite1.getUserId(),
                favorite1.getContentType(),
                favorite1.getContentId());
        then(movieRepository).should().existsById(favorite1.getContentId());
        then(favoriteRepository).should().save(any(Favorite.class));
    }

    @Test
    @DisplayName("중복된 즐겨찾기 추가 시 예외 발생")
    void createFavorite_Duplicate_ThrowsException() {
        given(userAccountRepository.findById(testUser.getId()))
                .willReturn(Optional.of(testUser));
        given(favoriteRepository.existsByUserAccountIdAndContentTypeAndContentId(
                favorite1.getUserId(),
                favorite1.getContentType(),
                favorite1.getContentId()))
                .willReturn(true);

        Throwable thrown = catchThrowable(() -> favoriteService.createFavorite(favorite1));

        assertThat(thrown)
                .isInstanceOf(ServiceException.class)
                .hasMessage("이미 즐겨찾기에 추가된 항목입니다.");
        then(userAccountRepository).should().findById(testUser.getId());
        then(favoriteRepository).should().existsByUserAccountIdAndContentTypeAndContentId(
                favorite1.getUserId(),
                favorite1.getContentType(),
                favorite1.getContentId());
    }

    @Test
    @DisplayName("존재하지 않는 User 추가 시 예외 발생")
    void createFavorite_UserNotFound_ThrowsException() {
        FavoriteDto favoriteDto = FavoriteDto.builder()
                .userId(9999L)
                .contentType(ContentType.MOVIE)
                .contentId(testMovie1.getId())
                .build();
        given(userAccountRepository.findById(favoriteDto.getUserId())).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> favoriteService.createFavorite(favoriteDto));

        assertThat(thrown)
                .isInstanceOf(ServiceException.class)
                .hasMessage("9999번 유저를 찾을 수 없습니다.");
        then(userAccountRepository).should().findById(favoriteDto.getUserId());
    }

    @Test
    @DisplayName("존재하지 않는 Content 추가 시 예외 발생")
    void createFavorite_ContentNotFound_ThrowsException() {
        FavoriteDto favoriteDto = FavoriteDto.builder()
                .userId(testUser.getId())
                .contentType(ContentType.MOVIE)
                .contentId(999999999L)
                .build();
        given(userAccountRepository.findById(any(Long.class))).willReturn(Optional.of(testUser));
        given(favoriteRepository.existsByUserAccountIdAndContentTypeAndContentId(
                any(Long.class),
                any(ContentType.class),
                any(Long.class))).willReturn(false);
        given(movieRepository.existsById(any(Long.class))).willReturn(false);

        Throwable thrown = catchThrowable(() -> favoriteService.createFavorite(favoriteDto));

        assertThat(thrown)
                .isInstanceOf(ServiceException.class)
                .hasMessage("%d번 컨텐츠를 찾을 수 없습니다.".formatted(favoriteDto.getContentId()));
        then(userAccountRepository).should().findById(any(Long.class));
        then(favoriteRepository).should().existsByUserAccountIdAndContentTypeAndContentId(
                any(Long.class),
                any(ContentType.class),
                any(Long.class));
        then(movieRepository).should().existsById(any(Long.class));
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - ID 정렬 및 페이징")
    void getUserFavorites_SortById() {
        // Given
        int page = 1, pageSize = 5;
        String sortBy = "id", direction = "desc";
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        given(userAccountRepository.existsById(favorite1.getUserId())).willReturn(true);
        given(favoriteRepository.findAllByUserAccountIdWithContent(any(Long.class), captor.capture()))
                .willReturn(new PageImpl<>(
                        List.of(favorite1.toEntity(testUser), favorite2.toEntity(testUser)),
                        PageRequest.of(
                                page - 1,
                                pageSize,
                                Sort.by(Sort.Direction.fromString(direction),
                                        "id")),
                        10));

        // When
        PageDto<FavoriteDto> favorites = favoriteService.getUserFavorites(
                testUser.getId(), page, pageSize, sortBy, direction);
        Pageable captured = captor.getValue();

        // Then
        assertThat(favorites).isNotNull();
        assertThat(favorites.getItems()).hasSize(2);
        assertThat(favorites.getItems()).flatExtracting(FavoriteDto::getUserId)
                .contains(favorite1.getUserId(), favorite2.getUserId());
        assertEquals(page - 1, captured.getPageNumber());
        assertEquals(pageSize, captured.getPageSize());
        assertEquals(Sort.by(Sort.Direction.fromString(direction), "id"), captured.getSort());
        then(userAccountRepository).should().existsById(favorite1.getUserId());
        then(favoriteRepository).should().findAllByUserAccountIdWithContent(any(Long.class), any(Pageable.class));
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - 평점 정렬")
    void getUserFavorites_SortByRating() {
        // Given
        int page = 1, pageSize = 2;
        String sortBy = "rating", direction = "asc";
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        given(userAccountRepository.existsById(favorite1.getUserId())).willReturn(true);
        given(favoriteRepository.findAllByUserAccountIdWithContent(any(Long.class), captor.capture()))
                .willReturn(new PageImpl<>(
                        List.of(),
                        PageRequest.of(
                                page - 1,
                                pageSize,
                                Sort.by(Sort.Direction.fromString(direction),
                                        "movie.averageRating",
                                        "series.averageRating")),
                        10));

        // When
        favoriteService.getUserFavorites(testUser.getId(), page, pageSize, sortBy, direction);
        Pageable captured = captor.getValue();

        // Then
        assertEquals(Sort.by(Sort.Direction.fromString(direction), "movie.averageRating",
                "series.averageRating"), captured.getSort());
        then(userAccountRepository).should().existsById(favorite1.getUserId());
        then(favoriteRepository).should().findAllByUserAccountIdWithContent(any(Long.class), any(Pageable.class));
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - 리뷰 개수 정렬")
    void getUserFavorites_SortByReviews() {
        // Given
        int page = 1, pageSize = 2;
        String sortBy = "reviews", direction = "desc";
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        given(userAccountRepository.existsById(favorite1.getUserId())).willReturn(true);
        given(favoriteRepository.findAllByUserAccountIdWithContent(any(Long.class), captor.capture()))
                .willReturn(new PageImpl<>(
                        List.of(),
                        PageRequest.of(
                                page - 1,
                                pageSize,
                                Sort.by(Sort.Direction.fromString(direction),
                                        "movie.ratingCount",
                                        "series.ratingCount")),
                        10));

        // When
        favoriteService.getUserFavorites(testUser.getId(), page, pageSize, sortBy, direction);
        Pageable captured = captor.getValue();

        // Then
        assertEquals(Sort.by(Sort.Direction.fromString(direction), "movie.ratingCount",
                "series.ratingCount"), captured.getSort());
        then(userAccountRepository).should().existsById(favorite1.getUserId());
        then(favoriteRepository).should().findAllByUserAccountIdWithContent(any(Long.class), any(Pageable.class));
    }

    @Test
    @DisplayName("잘못된 정렬 기준 입력 시 예외 발생")
    void getUserFavorites_InvalidSortBy_ThrowsException() {
        int page = 1, pageSize = 5;
        String sortBy = "unknown", direction = "descasd";
        given(userAccountRepository.existsById(favorite1.getUserId())).willReturn(true);

        Throwable thrown = catchThrowable(() ->
                favoriteService.getUserFavorites(testUser.getId(), page, pageSize, sortBy, direction));

        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid value '%s' for orders given; Has to be either 'desc' or 'asc' (case insensitive)".formatted(direction));
        then(userAccountRepository).should().existsById(favorite1.getUserId());
    }
}