package com.example.Flicktionary.domain.series.service;

import com.example.Flicktionary.domain.actor.entity.Actor;
import com.example.Flicktionary.domain.actor.repository.ActorRepository;
import com.example.Flicktionary.domain.director.entity.Director;
import com.example.Flicktionary.domain.director.repository.DirectorRepository;
import com.example.Flicktionary.domain.genre.entity.Genre;
import com.example.Flicktionary.domain.genre.repository.GenreRepository;
import com.example.Flicktionary.domain.series.dto.SeriesDetailResponse;
import com.example.Flicktionary.domain.series.entity.Series;
import com.example.Flicktionary.domain.series.entity.SeriesCast;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
import com.example.Flicktionary.domain.tmdb.dto.TmdbSeriesResponseWithDetail;
import com.example.Flicktionary.domain.tmdb.service.TmdbService;
import com.example.Flicktionary.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeriesService {
    private final SeriesRepository seriesRepository;
    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;
    private final TmdbService tmdbService;
    private final String BASE_IMAGE_URL = "https://image.tmdb.org/t/p";

    //Series 목록 조회(검색, 페이징, 정렬)
    @Transactional(readOnly = true)
    public Page<Series> getSeries(String keyword, int page, int pageSize, String sortBy) {
        Sort sort = getSort(sortBy);

        if (page < 1) {
            throw new ServiceException(HttpStatus.BAD_REQUEST.value(), "페이지는 1부터 요청 가능합니다.");
        }

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        return seriesRepository.findByTitleLike(keyword, pageable);
    }

    private Sort getSort(String sortBy) {
        return switch (sortBy) {
            case "id" -> Sort.by(Sort.Direction.ASC, "id");
            case "rating" -> Sort.by(Sort.Direction.DESC, "averageRating");
            case "ratingCount" -> Sort.by(Sort.Direction.DESC, "ratingCount");
            default -> throw new ServiceException(HttpStatus.BAD_REQUEST.value(), "잘못된 정렬 기준입니다.");
        };
    }

    //Series 상세 조회
    @Transactional(readOnly = true)
    public SeriesDetailResponse getSeriesDetail(Long id) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new ServiceException(HttpStatus.NOT_FOUND.value(), "%d번 시리즈를 찾을 수 없습니다.".formatted(id)));

        return new SeriesDetailResponse(series);
    }

    @Transactional
    public void fetchAndSaveSeries(int pages) {
        for (int i = 1; i <= pages; i++) {
            List<TmdbSeriesResponseWithDetail> seriesDtos = tmdbService.fetchSeries(i);

            for (TmdbSeriesResponseWithDetail seriesDto : seriesDtos) {
                if (seriesRepository.findByTmdbId(seriesDto.tmdbId()).isPresent()) {
                    continue;
                }

                Series series = Series.builder()
                        .tmdbId(seriesDto.tmdbId())
                        .title(seriesDto.title())
                        .overview(seriesDto.overview())
                        .releaseStartDate(seriesDto.releaseStartDate() == null || seriesDto.releaseStartDate().isEmpty()
                                ? null : LocalDate.parse(seriesDto.releaseStartDate()))
                        .releaseEndDate(seriesDto.releaseEndDate() == null || seriesDto.releaseEndDate().isEmpty()
                                ? null : LocalDate.parse(seriesDto.releaseEndDate()))
                        .status(seriesDto.status())
                        .posterPath(seriesDto.posterPath() == null ?
                                null : BASE_IMAGE_URL + "/w342" + seriesDto.posterPath())
                        .episodeNumber(seriesDto.numberOfEpisodes())
                        .productionCountry(seriesDto.productionCountries().isEmpty() ?
                                null : seriesDto.productionCountries().get(0).name())
                        .productionCompany(seriesDto.productionCompanies().isEmpty() ?
                                null : seriesDto.productionCompanies().get(0).name())
                        .build();

                // 장르 저장
                for (TmdbSeriesResponseWithDetail.TmdbGenre tmdbGenre : seriesDto.genres()) {
                    Genre genre = genreRepository.findById(tmdbGenre.id())
                            .orElseGet(() -> genreRepository.save(new Genre(tmdbGenre.id(), tmdbGenre.name())));
                    series.getGenres().add(genre);
                }

                // 배우 저장
                for (TmdbSeriesResponseWithDetail.TmdbActor tmdbActor : seriesDto.credits().cast().stream().limit(5).toList()) {
                    Actor actor = actorRepository.findById(tmdbActor.id())
                            .orElseGet(() -> actorRepository.save(new Actor(tmdbActor.id(), tmdbActor.name(),
                                    tmdbActor.profilePath() == null ? null : BASE_IMAGE_URL + "/w185" + tmdbActor.profilePath())));

                    SeriesCast seriesCast = SeriesCast.builder()
                            .series(series)
                            .actor(actor)
                            .characterName(tmdbActor.character())
                            .build();
                    series.getCasts().add(seriesCast);
                }

                // 감독 저장
                for (TmdbSeriesResponseWithDetail.TmdbCrew crew : seriesDto.credits().crew()) {
                    if (crew.job().equalsIgnoreCase("Director")) {
                        Director director = directorRepository.findById(crew.id())
                                .orElseGet(() -> directorRepository.save(new Director(crew.id(), crew.name(),
                                        crew.profilePath() == null ? null : BASE_IMAGE_URL + "/w185" + crew.profilePath())));
                        series.setDirector(director);

                        if (!director.getSeries().contains(series)) {  // 중복 추가 방지
                            director.getSeries().add(series);
                        }
                        series.setDirector(director);
                    }
                }

                seriesRepository.save(series);
            }
        }
    }
}
