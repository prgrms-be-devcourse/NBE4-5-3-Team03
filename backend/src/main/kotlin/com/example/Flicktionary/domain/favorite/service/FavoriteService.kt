package com.example.Flicktionary.domain.favorite.service

import com.example.Flicktionary.domain.favorite.dto.FavoriteContentDto
import com.example.Flicktionary.domain.favorite.dto.FavoriteDto
import com.example.Flicktionary.domain.favorite.dto.FavoriteDto.Companion.fromEntity
import com.example.Flicktionary.domain.favorite.entity.ContentType
import com.example.Flicktionary.domain.favorite.repository.FavoriteRepository
import com.example.Flicktionary.domain.movie.repository.MovieRepository
import com.example.Flicktionary.domain.series.repository.SeriesRepository
import com.example.Flicktionary.domain.user.repository.UserAccountRepository
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.exception.ServiceException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FavoriteService(
    private val favoriteRepository: FavoriteRepository,
    private val userAccountRepository: UserAccountRepository,
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository
) {
    @Transactional
    fun createFavorite(favoriteDto: FavoriteDto): FavoriteDto {
        val user = userAccountRepository.findById(favoriteDto.userId)
            .orElseThrow {
                ServiceException(
                    HttpStatus.NOT_FOUND.value(),
                    "${favoriteDto.userId}번 유저를 찾을 수 없습니다."
                )
            }

        // 즐겨찾기 목록에 존재하는지 확인
        val exists = favoriteRepository.existsByUserAccountIdAndContentTypeAndContentId(
            favoriteDto.userId,
            favoriteDto.contentType,
            favoriteDto.contentId
        )

        if (exists) {
            throw ServiceException(HttpStatus.CONFLICT.value(), "이미 즐겨찾기에 추가된 항목입니다.")
        }

        // contentType에 따라 contentId 검증
        val contentExists = when (favoriteDto.contentType) {
            ContentType.MOVIE -> movieRepository.existsById(favoriteDto.contentId)
            ContentType.SERIES -> seriesRepository.existsById(favoriteDto.contentId)
        }

        if (!contentExists) {
            throw ServiceException(
                HttpStatus.NOT_FOUND.value(),
                "${favoriteDto.contentId}번 컨텐츠를 찾을 수 없습니다."
            )
        }

        val favorite = favoriteDto.toEntity(user)

        favoriteRepository.save(favorite)
        return fromEntity(favorite)
    }

    @Transactional(readOnly = true)
    fun getUserFavorites(
        userId: Long,
        page: Int,
        pageSize: Int,
        sortBy: String,
        direction: String
    ): PageDto<FavoriteDto> {
        if (!userAccountRepository.existsById(userId)) {
            throw ServiceException(HttpStatus.NOT_FOUND.value(), "${userId}번 유저를 찾을 수 없습니다.")
        }

        val sort = when (sortBy) {
            "rating" -> Sort.by(Sort.Direction.fromString(direction), "movie.averageRating", "series.averageRating")
            "reviews" -> Sort.by(Sort.Direction.fromString(direction), "movie.ratingCount", "series.ratingCount")
            else -> Sort.by(Sort.Direction.fromString(direction), "id")
        }

        val pageable: Pageable = PageRequest.of(page - 1, pageSize, sort)
        val favorites = favoriteRepository.findAllByUserAccountIdWithContent(userId, pageable)

        // DTO 변환 후 수동 정렬 수행 (ASC/DESC 지원)
        val comparator = Comparator.comparing<FavoriteDto, Double> { favoriteDto ->
            getSortValue(favoriteDto, sortBy)
        }.let { if (direction.equals("DESC", ignoreCase = true)) it.reversed() else it }

        val sortedFavorites = favorites
            .map(FavoriteDto::fromEntity)
            .sortedWith(comparator)

        return PageDto(PageImpl(sortedFavorites, pageable, favorites.totalElements))
    }

    // 정렬 기준 값을 가져오는 메서드
    private fun getSortValue(favoriteDto: FavoriteDto, sortBy: String): Double {
        val contentDto = favoriteDto.data as? FavoriteContentDto
        return when (sortBy) {
            "rating" -> contentDto?.averageRating ?: 0.0
            "reviews" -> contentDto?.ratingCount?.toDouble() ?: 0.0
            else -> favoriteDto.id.toDouble()
        }
    }

    @Transactional
    fun deleteFavorite(id: Long) {
        val favorite = favoriteRepository.findById(id)
            .orElseThrow {
                ServiceException(
                    HttpStatus.NOT_FOUND.value(),
                    "${id}번 즐겨찾기 정보를 찾을 수 없습니다."
                )
            }

        favoriteRepository.delete(favorite)
    }
}
