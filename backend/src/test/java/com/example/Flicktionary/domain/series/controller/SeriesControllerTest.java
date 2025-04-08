package com.example.Flicktionary.domain.series.controller;

import com.example.Flicktionary.domain.series.dto.SeriesDetailResponse;
import com.example.Flicktionary.domain.series.dto.SeriesSummaryResponse;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.service.SeriesService;
import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService;
import com.example.Flicktionary.domain.user.service.UserAccountService;
import com.example.Flicktionary.global.dto.PageDto;
import com.example.Flicktionary.global.security.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("시리즈 도메인 컨트롤러 테스트")
@Import({SeriesService.class,
        UserAccountService.class,
        UserAccountJwtAuthenticationService.class,
        CustomUserDetailsService.class})
@WebMvcTest(SeriesController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SeriesControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SeriesService seriesService;

    @MockitoBean
    private UserAccountService userAccountService;

    @MockitoBean
    private UserAccountJwtAuthenticationService userAccountJwtAuthenticationService;

    private Series series1, series2, series3;

    @BeforeEach
    void setUp() {
        series1 = new Series(1L, "Series 1", "",
                LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1),
                "", "series.png", 10, "", "");
        series1.setId(1L);
        series1.setAverageRating(2.1);
        series1.setRatingCount(150);

        series2 = new Series(2L, "Series 2", "",
                LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1),
                "", "series.png", 10, "", "");
        series2.setId(2L);
        series2.setAverageRating(3.6);
        series2.setRatingCount(100);

        series3 = new Series(3L, "Series 3", "",
                LocalDate.of(2022, 1, 1), LocalDate.of(2023, 1, 1),
                "", "series.png", 10, "", "");
        series3.setId(3L);
        series3.setAverageRating(3.0);
        series3.setRatingCount(50);
    }

    @Test
    @DisplayName("Series 목록 조회")
    void getSeriesTest() throws Exception {
        // 테스트할 파라미터 설정
        String keyword = "";
        int page = 1;
        int pageSize = 2;
        String sortBy = "id";

        //given
        List<Series> mockSeriesList = List.of(
            series1, series2, series3
        );
        Page<Series> mockSeriesPage = new PageImpl<>(mockSeriesList, PageRequest.of(page - 1, pageSize), mockSeriesList.size());
        PageDto<SeriesSummaryResponse> result = new PageDto<>(mockSeriesPage.map(SeriesSummaryResponse::new));

        // mockSeriesPage를 seriesService.getSeries()에서 반환하도록 설정(when)
        when(seriesService.getSeries(keyword, page, pageSize, sortBy)).thenReturn(mockSeriesPage);
        ResultActions resultActions = mvc.perform(get("/api/series")
                        .param("keyword", keyword)
                        .param("page", String.valueOf(page))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("sortBy", sortBy)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // 예상 반환값과 API 요청 반환 값 비교(then)
        resultActions
                .andExpect(status().isOk())  // HTTP 상태 코드가 200 OK인지 확인
                .andExpect(handler().handlerType(SeriesController.class))  // 호출된 핸들러가 SeriesController인지 확인
                .andExpect(handler().methodName("getSeries"))  // 호출된 메서드가 getSeries인지 확인
                .andExpect(jsonPath("$.data.items").isArray())  // 응답의 items가 배열인지 확인
                .andExpect(jsonPath("$.data.items[0].id").value(result.getItems().get(0).getId()))  // 첫 번째 아이템의 ID 검증
                .andExpect(jsonPath("$.data.items[1].id").value(result.getItems().get(1).getId()))  // 두 번째 아이템의 ID 검증
                .andExpect(jsonPath("$.data.totalPages").value(result.getTotalPages()))  // 전체 페이지 수 검증
                .andExpect(jsonPath("$.data.totalItems").value(result.getTotalItems()));  // 전체 아이템 수 검증
    }

    @Test
    @DisplayName("Series 상세 조회")
    void getSeriesDetailTest() throws Exception {
        // given
        Long seriesId = 1L;
        SeriesDetailResponse response = new SeriesDetailResponse(
                seriesId, 1L, "Test Series", "http://test.com/image.jpg",
                4.5, 100, 10, "Test Plot", "Test Company", "Test Nation",
                LocalDate.of(2020, 1, 1), LocalDate.of(2021, 1, 1),
                "Completed", Collections.emptyList(), Collections.emptyList(), null
        );

        //when
        when(seriesService.getSeriesDetail(seriesId)).thenReturn(response);

        //then
        mvc.perform(get("/api/series/{id}", seriesId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(seriesId))
                .andExpect(jsonPath("$.data.title").value("Test Series"))
                .andExpect(jsonPath("$.data.averageRating").value(4.5))
                .andExpect(jsonPath("$.data.ratingCount").value(100))
                .andExpect(jsonPath("$.data.episode").value(10))
                .andExpect(jsonPath("$.data.plot").value("Test Plot"))
                .andExpect(jsonPath("$.data.company").value("Test Company"))
                .andExpect(jsonPath("$.data.nation").value("Test Nation"))
                .andExpect(jsonPath("$.data.status").value("Completed"))
                .andDo(print());
    }
}
