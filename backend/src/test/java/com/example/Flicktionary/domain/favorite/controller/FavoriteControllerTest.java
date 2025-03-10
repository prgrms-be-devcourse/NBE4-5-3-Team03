package com.example.Flicktionary.domain.favorite.controller;

import com.example.Flicktionary.domain.favorite.dto.FavoriteDto;
import com.example.Flicktionary.domain.favorite.entity.ContentType;
import com.example.Flicktionary.domain.favorite.service.FavoriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
//@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class FavoriteControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private FavoriteService favoriteService;

    private FavoriteDto favoriteDto;

//    @Autowired
//    private UserAccountRepository userAccountRepository;
//    @Autowired
//    private MovieRepository movieRepository;
//    @Autowired
//    private SeriesRepository seriesRepository;
//
//    private UserAccount testUser;
//    private Movie testMovie;
//    private Series testSeries;
//    private ReviewDto reviewDto;
//
//    @BeforeEach
//    void beforeEach() {
//
//        // 테스트용 User 엔티티 생성 및 저장
//        testUser = userAccountRepository.save(UserAccount.builder()
//                .username("테스트용 유저")
//                .password("test1234")
//                .email("test@email.com")
//                .nickname("테스트 유저")
//                .role(UserAccountType.USER)
//                .build());
//
//        // 테스트용 Movie 엔티티 생성
//        testMovie = movieRepository.save(Movie.builder()
//                .tmdbId(10000000000L)
//                .title("테스트용 영화 제목")
//                .overview("테스트용 영화 줄거리")
//                .releaseDate(LocalDate.of(2024, 1, 1))
//                .posterPath("테스트용 이미지")
//                .productionCountry("KR")
//                .productionCompany("테스트용 제작사")
//                .status("상영 중")
//                .averageRating(4)
//                .ratingCount(15)
//                .build()
//        );
//
//        // 테스트용 Series 엔티티 생성
//        testSeries = seriesRepository.save(Series.builder()
//                .tmdbId(1L)
//                .title("테스트용 드라마 제목")
//                .plot("테스트용 드라마 줄거리")
//                .episode(12)
//                .status("상영중")
//                .imageUrl("테스트용 이미지")
//                .averageRating(4.5)
//                .ratingCount(10)
//                .releaseStartDate(LocalDate.of(2024, 1, 1))
//                .releaseEndDate(LocalDate.of(2200, 1, 2))
//                .nation("KR")
//                .company("테스트용 제작사")
//                .build()
//        );
//
//        reviewDto = ReviewDto.builder()
//                .userAccountId(testUser.getId())
//                .nickname(testUser.getNickname())
//                .movieId(testMovie.getId())
//                .rating(5)
//                .content("테스트용 리뷰 내용")
//                .build();
//    }

    @Test
    @DisplayName("Create Favorite")
    void createFavorite() throws Exception {

        Long userId = 1L;
        ContentType contentType = ContentType.MOVIE;
        Long contentId = 599L;

        ResultActions resultActions = mvc
                .perform(
                        post("/api/favorite")
                                .content("""
                                        {
                                            "userId": "%d",
                                            "contentType": "%s",
                                            "contentId": "%d"
                                        }
                                        """
                                        .formatted(userId, contentType, contentId)
                                        .stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isCreated());

    }

    @Test
    @DisplayName("Read Favorite")
    void getFavorite() throws Exception {

        Long userId = 1L;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/favorite/%d".formatted(userId))
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Delete Favorite")
    void deleteFavorite() throws Exception {

        Long id = 1L;

        ResultActions resultActions = mvc
                .perform(
                        delete("/api/favorite/%d".formatted(id))
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("중복 저장 테스트")
    void checkDuplicate() throws Exception {
        Long userId = 1L;
        ContentType contentType = ContentType.MOVIE;
        Long contentId = 200L;

        // 첫 번째 저장 (201 Created)
        String content = """
                {
                    "userId": "%d",
                    "contentType": "%s",
                    "contentId": "%d"
                }
                """
                .formatted(userId, contentType, contentId)
                .stripIndent();


        mvc.perform(
                        post("/api/favorite")
                                .content(content)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isCreated());

        // 중복 저장 시도 (IllegalArgumentException 발생, 400 Bad Request)
        mvc.perform(
                        post("/api/favorite")
                                .content(content)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 ContentID 저장 테스트")
    void checkContentId() throws Exception {
        Long userId = 1L;
        ContentType contentType = ContentType.MOVIE;
        Long contentId = 9999L;

        // 존재하지 않는 ContentID 저장 시도 (IllegalArgumentException 발생, 400 Bad Request)
        mvc.perform(
                        post("/api/favorite")
                                .content("""
                                        {
                                            "userId": "%d",
                                            "contentType": "%s",
                                            "contentId": "%d"
                                        }
                                        """
                                        .formatted(userId, contentType, contentId)
                                        .stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 User 저장 테스트")
    void checkUserId() throws Exception {
        Long userId = 9999L;
        ContentType contentType = ContentType.MOVIE;
        Long contentId = 1L;

        // 존재하지 않는 User 저장 시도 (IllegalArgumentException 발생, 400 Bad Request)
        mvc.perform(
                        post("/api/favorite")
                                .content("""
                                        {
                                            "userId": "%d",
                                            "contentType": "%s",
                                            "contentId": "%d"
                                        }
                                        """
                                        .formatted(userId, contentType, contentId)
                                        .stripIndent())
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Read Favorite - Page, id 내림차순 정렬(기본)")
    void getFavoritePage() throws Exception {

        Long userId = 1L;
        int page = 1;
        int pageSize = 5;

        ResultActions resultActions = mvc
                .perform(
                        get("/api/favorite/%d?page=%d&pageSize=%d".formatted(userId, page, pageSize))
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curPageNo").value(page))
                .andExpect(jsonPath("$.pageSize").value(pageSize))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("Read Favorite - Page, id 오름차순 정렬")
    void getFavoritePageSortByIdDesc() throws Exception {

        Long userId = 1L;
        int page = 1;
        int pageSize = 5;
        String sortBy = "id";
        String direction = "asc";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/favorite/{userId}", userId)
                                .param("page", "%d".formatted(page))
                                .param("pageSize", "%d".formatted(pageSize))
                                .param("sortBy", sortBy)
                                .param("direction", direction)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curPageNo").value(page))
                .andExpect(jsonPath("$.pageSize").value(pageSize))
                .andExpect(jsonPath("$.sortBy").exists())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("Read Favorite - Page, rating 내림차순 정렬")
    void getFavoritePageSortByRatingDesc() throws Exception {

        Long userId = 1L;
        int page = 1;
        int pageSize = 5;
        String sortBy = "rating";
        String direction = "desc";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/favorite/{userId}", userId)
                                .param("page", "%d".formatted(page))
                                .param("pageSize", "%d".formatted(pageSize))
                                .param("sortBy", sortBy)
                                .param("direction", direction)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curPageNo").value(page))
                .andExpect(jsonPath("$.pageSize").value(pageSize))
                .andExpect(jsonPath("$.sortBy").exists())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("Read Favorite - Page, rating 오름차순 정렬")
    void getFavoritePageSortByRatingAsc() throws Exception {

        Long userId = 1L;
        int page = 1;
        int pageSize = 5;
        String sortBy = "rating";
        String direction = "asc";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/favorite/{userId}", userId)
                                .param("page", "%d".formatted(page))
                                .param("pageSize", "%d".formatted(pageSize))
                                .param("sortBy", sortBy)
                                .param("direction", direction)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curPageNo").value(page))
                .andExpect(jsonPath("$.pageSize").value(pageSize))
                .andExpect(jsonPath("$.sortBy").exists())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("Read Favorite - Page, reviews 내림차순 정렬")
    void getFavoritePageSortByReviewsDesc() throws Exception {

        Long userId = 1L;
        int page = 1;
        int pageSize = 5;
        String sortBy = "reviews";
        String direction = "desc";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/favorite/{userId}", userId)
                                .param("page", "%d".formatted(page))
                                .param("pageSize", "%d".formatted(pageSize))
                                .param("sortBy", sortBy)
                                .param("direction", direction)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curPageNo").value(page))
                .andExpect(jsonPath("$.pageSize").value(pageSize))
                .andExpect(jsonPath("$.sortBy").exists())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("Read Favorite - Page, reviews 오름차순 정렬")
    void getFavoritePageSortByReviewsAsc() throws Exception {

        Long userId = 1L;
        int page = 1;
        int pageSize = 5;
        String sortBy = "reviews";
        String direction = "asc";

        ResultActions resultActions = mvc
                .perform(
                        get("/api/favorite/{userId}", userId)
                                .param("page", "%d".formatted(page))
                                .param("pageSize", "%d".formatted(pageSize))
                                .param("sortBy", sortBy)
                                .param("direction", direction)
                                .contentType(
                                        new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
                                )
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curPageNo").value(page))
                .andExpect(jsonPath("$.pageSize").value(pageSize))
                .andExpect(jsonPath("$.sortBy").exists())
                .andExpect(jsonPath("$.items").isArray());
    }
}
