package com.example.Flicktionary.domain.series.dto;

import com.example.Flicktionary.domain.genre.dto.GenreDto;
import com.example.Flicktionary.domain.genre.entity.Genre;
import com.example.Flicktionary.domain.series.entity.Series;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SeriesDetailDto {

    @JsonProperty("id")
    private Long tmdbId;

    private String name;

    private String overview;

    @JsonProperty("number_of_episodes")
    private int numberOfEpisodes;

    @JsonProperty("poster_path")
    private String posterPath;

    private List<GenreDto> genres;

    @JsonProperty("first_air_date")
    private String firstAirDate;

    @JsonProperty("last_air_date")
    private String lastAirDate;

    @JsonProperty("origin_country")
    private List<String> originCountry;

    private String status;

    @JsonProperty("production_companies")
    private List<ProductionCompanyDto> productionCompanies;


    public static Series toEntity(ResponseEntity<SeriesDetailDto> response, List<Genre> genres, String baseImageUrl) {
        SeriesDetailDto body = response.getBody();

        return Series.builder()
                .title(body.getName())
                .plot(body.getOverview())
                .episode(body.getNumberOfEpisodes())
                .status(body.getStatus())
                .imageUrl(baseImageUrl + body.getPosterPath())
                .releaseStartDate(parseDate(body.getFirstAirDate()))
                .releaseEndDate(parseDate(body.getLastAirDate()))
                .nation(getFirstOrDefault(body.getOriginCountry(), "Unknown"))
                .company(getFirstCompanyOrDefault(body.getProductionCompanies(), "Unknown"))
                .genres(genres)
                .tmdbId(body.getTmdbId())
                .build();
    }

    // 날짜 null 체크
    private static LocalDate parseDate(String date) {
        return (date != null && !date.isEmpty()) ? LocalDate.parse(date) : null;
    }

    // nation null 체크
    private static String getFirstOrDefault(List<String> list, String defaultValue) {
        return (list != null && !list.isEmpty()) ? list.get(0) : defaultValue;
    }

    // company null 체크
    private static String getFirstCompanyOrDefault(List<ProductionCompanyDto> list, String defaultValue) {
        return (list != null && !list.isEmpty()) ? list.get(0).getName() : defaultValue;
    }

    @Getter
    public static class ProductionCompanyDto {
        private String name;
    }
}
