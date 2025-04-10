package com.example.Flicktionary.domain.review.controller

import com.example.Flicktionary.domain.review.dto.ReviewDto
import com.example.Flicktionary.domain.review.service.ReviewService
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.dto.ResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reviews")
class ReviewController(
    private val reviewService: ReviewService
) {
    // 리뷰 생성
    @PostMapping
    fun createReview(
        @RequestBody reviewDto: ReviewDto
    ): ResponseEntity<ResponseDto<ReviewDto>> {
        val review = reviewService.createReview(reviewDto)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ResponseDto.of(HttpStatus.CREATED.value().toString(), HttpStatus.CREATED.reasonPhrase, review))
    }

    // 모든 리뷰 조회
    @GetMapping
    fun getAllReviews(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int
    ): ResponseEntity<ResponseDto<PageDto<ReviewDto>>> {
        val reviews = reviewService.findAllReviews(page, pageSize)
        return ResponseEntity.ok(ResponseDto.ok(reviews))
    }

    // 리뷰 닉네임과 내용으로 검색
    @GetMapping("/search")
    fun searchReviews(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") pageSize: Int
    ): ResponseEntity<ResponseDto<PageDto<ReviewDto>>> {
        return ResponseEntity.ok(
            ResponseDto.ok(
                reviewService.searchReviews(keyword, page, pageSize)
            )
        )
    }

    // 리뷰 수정
    @PutMapping("/{id}")
    fun updateReview(
        @PathVariable id: Long,
        @RequestBody reviewDto: ReviewDto
    ): ResponseEntity<ResponseDto<ReviewDto>> {
        val review = reviewService.updateReview(id, reviewDto)
        return ResponseEntity.ok(ResponseDto.ok(review))
    }

    // 리뷰 삭제
    @DeleteMapping("/{id}")
    fun deleteReview(
        @PathVariable id: Long
    ): ResponseEntity<ResponseDto<*>> {
        reviewService.deleteReview(id)
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(ResponseDto.of(HttpStatus.NO_CONTENT.value().toString(), HttpStatus.NO_CONTENT.reasonPhrase))
    }

    // TODO: 예외 처리를 서비스 레이어로 옮기는 것을 검토
    // TODO: Spring에서 제공하는 예외를 바로 던질지, 내부에서 따로 예외를 처리할지 여부를 검토
    // 특정 영화의 리뷰를 페이지로 조회 (0 ~ 5 페이지)
    @GetMapping("/movies/{movie_id}")
    fun reviewMovieDtoPage(
        @PathVariable("movie_id") movie_id: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") pageSize: Int
    ): ResponseEntity<ResponseDto<PageDto<ReviewDto>>> {
        // 특정 영화 리뷰 페이지를 조회해 변수에 담아 클라이언트에 반환
        val reviews = reviewService.reviewMovieDtoPage(movie_id, page, pageSize)
        return ResponseEntity.ok(ResponseDto.ok(reviews))
    }

    // 특정 드라마의 리뷰를 페이지로 조회 (0 ~ 5 페이지)
    @GetMapping("/series/{series_id}")
    fun reviewSeriesDtoPage(
        @PathVariable("series_id") series_id: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") pageSize: Int
    ): ResponseEntity<ResponseDto<PageDto<ReviewDto>>> {
        // 특정 드라마 리뷰 페이지를 조회해 변수에 담아 클라이언트에 반환
        val reviews = reviewService.reviewSeriesDtoPage(series_id, page, pageSize)
        return ResponseEntity.ok(ResponseDto.ok(reviews))
    }
}