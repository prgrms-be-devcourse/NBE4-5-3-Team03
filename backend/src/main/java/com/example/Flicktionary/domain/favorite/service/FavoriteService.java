package com.example.Flicktionary.domain.favorite.service;

import com.example.Flicktionary.domain.favorite.dto.FavoriteDto;
import com.example.Flicktionary.domain.favorite.entity.Favorite;
import com.example.Flicktionary.domain.favorite.repository.FavoriteRepository;
import com.example.Flicktionary.domain.movie.repository.MovieRepository;
import com.example.Flicktionary.domain.series.repository.SeriesRepository;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.repository.UserAccountRepository;
import com.example.Flicktionary.global.dto.PageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserAccountRepository userAccountRepository;
    private final MovieRepository movieRepository;
    private final SeriesRepository seriesRepository;

    @Transactional
    public FavoriteDto createFavorite(FavoriteDto favoriteDto) {
        UserAccount user = userAccountRepository.findById(favoriteDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 즐겨찾기 목록에 존재하는지 확인
        boolean exists = favoriteRepository.existsByUserAccountIdAndContentTypeAndContentId(
                favoriteDto.getUserId(),
                favoriteDto.getContentType(),
                favoriteDto.getContentId()
        );

        if (exists) {
            throw new IllegalArgumentException("이미 즐겨찾기에 추가된 항목입니다.");
        }

        // contentType에 따라 contentId 검증
        boolean contentExists = false;

        if ("MOVIE".equalsIgnoreCase(String.valueOf(favoriteDto.getContentType()))) {
            contentExists = movieRepository.existsById(favoriteDto.getContentId());
        } else if ("SERIES".equalsIgnoreCase(String.valueOf(favoriteDto.getContentType()))) {
            contentExists = seriesRepository.existsById(favoriteDto.getContentId());
        }

        if (!contentExists) {
            throw new IllegalArgumentException(favoriteDto.getContentId() + "번 ContentID를 찾을 수 없습니다.");
        }

        Favorite favorite = favoriteDto.toEntity(user);

        favoriteRepository.save(favorite);
        return FavoriteDto.fromEntity(favorite);
    }

    public PageDto<FavoriteDto> getUserFavorites(Long userId, int page, int pageSize, String sortBy, String direction) {
        if (!userAccountRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        Sort sort = switch (sortBy) {
            case "rating" ->
                    Sort.by(Sort.Direction.fromString(direction), "movie.averageRating", "series.averageRating");
            case "reviews" -> Sort.by(Sort.Direction.fromString(direction), "movie.ratingCount", "series.ratingCount");
            default -> Sort.by(Sort.Direction.fromString(direction), "id");
        };

        Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
        Page<Favorite> favorites = favoriteRepository.findAllByUserAccountIdWithContent(userId, pageable);

        Page<FavoriteDto> favoriteDtos = favorites.map(FavoriteDto::fromEntity);
        return new PageDto<>(favoriteDtos);
    }

    @Transactional
    public void deleteFavorite(Long id) {
        favoriteRepository.deleteById(id);
    }

}
