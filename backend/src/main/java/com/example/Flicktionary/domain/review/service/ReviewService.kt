package com.example.Flicktionary.domain.review.service

import com.example.Flicktionary.domain.movie.entity.Movie
import com.example.Flicktionary.domain.movie.repository.MovieRepository
import com.example.Flicktionary.domain.review.dto.ReviewDto
import com.example.Flicktionary.domain.review.entity.Review
import com.example.Flicktionary.domain.review.repository.ReviewRepository
import com.example.Flicktionary.domain.series.entity.Series
import com.example.Flicktionary.domain.series.repository.SeriesRepository
import com.example.Flicktionary.domain.user.entity.UserAccount
import com.example.Flicktionary.domain.user.repository.UserAccountRepository
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.exception.ServiceException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ReviewService (
    private val reviewRepository: ReviewRepository,
    private val userAccountRepository: UserAccountRepository,
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository
) {

    // 리뷰 생성
    fun createReview(reviewDto: ReviewDto): ReviewDto {
        val userAccountId = reviewDto.userAccountId
        val movieId = reviewDto.movieId
        val seriesId = reviewDto.seriesId

        // 유저 id를 찾음
        val userAccount = userAccountRepository.findById(reviewDto.userAccountId!!)
            .orElseThrow {
                ServiceException(
                    HttpStatus.NOT_FOUND.value(),
                    "${userAccountId}번 유저를 찾을 수 없습니다."
                )
            }

        // 특정 영화에 이미 리뷰를 작성했는지 확인
        if (movieId != null && reviewRepository.existsByUserAccount_IdAndMovie_Id(userAccountId!!, movieId)) {
            throw ServiceException(HttpStatus.CONFLICT.value(), "이미 해당 영화에 대한 리뷰를 작성하셨습니다.")
        }

        // 특정 드라마에 이미 리뷰를 작성했는지 확인
        if (seriesId != null && reviewRepository.existsByUserAccount_IdAndSeries_Id(userAccountId!!, seriesId)) {
            throw ServiceException(HttpStatus.CONFLICT.value(), "이미 해당 드라마에 대한 리뷰를 작성하셨습니다.")
        }

        // 리뷰 내용이 null이거나 비어있을 경우
        if (reviewDto.content.isNullOrBlank()) {
            throw ServiceException(HttpStatus.BAD_REQUEST.value(), "리뷰 내용을 입력해주세요.")
        }

        // 평점이 매겨지지 않을 경우
        if (reviewDto.rating == 0) {
            throw ServiceException(HttpStatus.BAD_REQUEST.value(), "평점을 매겨주세요.")
        }

        // 영화를 찾아 저장. 없을 경우 null
        val movie: Movie? = reviewDto.movieId?.let {
            movieRepository.findById(it).orElse(null)
        }

        // 드라마를 찾아 저장. 없을 경우 null
        val series: Series? = reviewDto.seriesId?.let {
            seriesRepository.findById(it).orElse(null)
        }

        // ReviewDto를 Entity로 변환해 변수에 저장
        val review = reviewDto.toEntity(userAccount, movie, series)

        // 레포지터리에 DB 영속화 및 변수에 저장
        val savedReview = reviewRepository.save(review)

        // 영화와 시리즈의 정보 업데이트
        updateRatingAndCount(movie, series, review.rating, true)

        return ReviewDto.fromEntity(savedReview)
    }

    // 모든 리뷰 조회
    fun findAllReviews(page: Int, size: Int): PageDto<ReviewDto> {
        // 모든 리뷰를 찾아 리턴
        val pageable: Pageable = PageRequest.of(page, size, Sort.by("id").descending())
        val reviewDtoPage: Page<ReviewDto> = reviewRepository.findAll(pageable)
            .map {
                ReviewDto.fromEntity(it)
            }
        return PageDto(reviewDtoPage)
    }

    // 리뷰 닉네임과 내용으로 검색
    fun searchReviews(keyword: String, page: Int, size: Int): PageDto<ReviewDto> {
        // Pageable 변수로 페이지와 크기를 받아 변수에 저장
        val pageable: Pageable = PageRequest.of(page, size)

        // 닉네임 또는 리뷰 내용에 검색어가 포함된 리뷰 조회
        val reviewPage = reviewRepository
            .findByUserAccount_NicknameContainingOrContentContaining(keyword, keyword, pageable)

        return PageDto<ReviewDto>(reviewPage
            .map {
                ReviewDto.fromEntity(it)
            }
        )
    }

    // 리뷰 수정
    fun updateReview(id: Long, reviewDto: ReviewDto): ReviewDto {
        // id로 리뷰를 찾을 수 없을 경우
        val review = reviewRepository.findById(id)
            .orElseThrow {
                ServiceException(
                    HttpStatus.NOT_FOUND.value(),
                    "${id}번 리뷰를 찾을 수 없습니다."
                )
            }

        // 평점을 수정한다면 영화와 시리즈 정보 업데이트
        if (reviewDto.rating != 0 && reviewDto.rating != review.rating) {
            updateRatingAndCount(review.movie, review.series, reviewDto.rating - review.rating, false)
            review.rating = reviewDto.rating
        }

        // 리뷰의 평점 수정
        if (reviewDto.rating != 0) {
            review.rating = reviewDto.rating
        }

        // 리뷰의 내용 수정
        if (!reviewDto.content.isNullOrBlank()) {
            review.content = reviewDto.content
        }

        return ReviewDto.fromEntity(reviewRepository.save(review))
    }

    // 리뷰 삭제
    fun deleteReview(id: Long) {
        // id로 리뷰를 찾을 수 없을 경우
        val review = reviewRepository.findById(id)
            .orElseThrow {
                ServiceException(
                    HttpStatus.NOT_FOUND.value(),
                    "${id}번 리뷰를 찾을 수 없습니다."
                )
            }

        // 영화, 시리즈 정보 업데이트
        updateRatingAndCount(review.movie, review.series, -review.rating, true)

        reviewRepository.delete(review)
    }

    // 공통 평점 업데이트 메서드
    private fun updateRatingAndCount(movie: Movie?, series: Series?, ratingChange: Int, isAddOrDelete: Boolean) {
        movie?.let {
            val newRatingCount = if (isAddOrDelete)
                it.ratingCount + (if (ratingChange > 0) 1 else -1)
            else it.ratingCount
            val newAverageRating = if (newRatingCount == 0) 0.0
            else (it.averageRating * it.ratingCount + ratingChange) / newRatingCount
            it.ratingCount = newRatingCount
            it.averageRating = newAverageRating
        }

        series?.let {
            val newRatingCount = if (isAddOrDelete)
                it.ratingCount + (if (ratingChange > 0) 1 else -1)
            else it.ratingCount
            val newAverageRating = if (newRatingCount == 0) 0.0
            else (it.averageRating * it.ratingCount + ratingChange) / newRatingCount
            it.ratingCount = newRatingCount
            it.averageRating = newAverageRating
        }
    }

    // 페이지네이션을 이용해서 특정 영화의 리뷰 목록을 조회
    fun reviewMovieDtoPage(movieId: Long, page: Int, size: Int): PageDto<ReviewDto> {
        // Pageable 변수로 페이지와 크기를 받아 변수에 저장
        val pageable: Pageable = PageRequest.of(page, size)

        // 영화 id로 영화를 찾아 ReviewDto 객체 목록으로 변환하여, Page 변수에 담아 return
        val reviewDtoPage: Page<ReviewDto> = reviewRepository.findByMovie_IdOrderByIdDesc(movieId, pageable)
            .map {
                ReviewDto.fromEntity(it)
            }

        return PageDto(reviewDtoPage)
    }

    // 페이지네이션을 이용해서 특정 드라마의 리뷰 목록을 조회
    fun reviewSeriesDtoPage(seriesId: Long, page: Int, size: Int): PageDto<ReviewDto> {
        // Pageable 변수로 페이지와 크기를 받아 변수에 저장
        val pageable: Pageable = PageRequest.of(page, size)

        // 드라마 id로 드라마를 찾아 ReviewDto 객체 목록으로 변환하여, Page 변수에 담아 return
        val reviewDtoPage: Page<ReviewDto> = reviewRepository.findBySeries_IdOrderByIdDesc(seriesId, pageable)
            .map {
                ReviewDto.fromEntity(it)
            }

        return PageDto(reviewDtoPage)
    }

    // 특정 유저의 모든 리뷰에서 userAccount를 null로 설정하는 메서드
    fun disassociateReviewsFromUser(userAccount: UserAccount?) {
        val reviewsToUpdate: List<Review> = reviewRepository.findByUserAccount(userAccount)

        reviewsToUpdate.forEach {
            it.userAccount = null
        }

        reviewRepository.saveAll(reviewsToUpdate)
    }
}
