package com.example.Flicktionary.domain.series.controller

import com.example.Flicktionary.domain.series.dto.SeriesDetailResponse
import com.example.Flicktionary.domain.series.dto.SeriesRequest
import com.example.Flicktionary.domain.series.dto.SeriesSummaryResponse
import com.example.Flicktionary.domain.series.service.SeriesService
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.dto.ResponseDto
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/series")
class SeriesController(
    private val seriesService: SeriesService
) {
    //Series 목록 조회(페이징, 정렬)
    @GetMapping
    fun getSeries(
        @RequestParam(defaultValue = "") keyword: String,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "id") sortBy: String
    ): ResponseEntity<ResponseDto<PageDto<SeriesSummaryResponse>>> {
        val series = seriesService.getSeries(keyword, page, pageSize, sortBy)
        val pageDto = PageDto(series.map {
            SeriesSummaryResponse(it)
        })
        return ResponseEntity.ok(ResponseDto.ok(pageDto))
    }

    //Series 상세 목록 조회
    @GetMapping("/{id}")
    fun getSeriesDetail(@PathVariable id: Long): ResponseEntity<ResponseDto<SeriesDetailResponse>> {
        val seriesDto = seriesService.getSeriesDetail(id)
        return ResponseEntity.ok(ResponseDto.ok(seriesDto))
    }

    @Operation(summary = "시리즈 생성", description = "시리즈를 생성합니다. 관리자만 접근 가능합니다.")
    @PostMapping
    fun createSeries(@RequestBody @Valid request: SeriesRequest): ResponseEntity<ResponseDto<SeriesDetailResponse>> {
        val response = seriesService.createSeries(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ResponseDto.of(HttpStatus.CREATED.value().toString(), HttpStatus.CREATED.reasonPhrase, response))
    }

    @Operation(summary = "시리즈 수정", description = "시리즈를 수정합니다. 관리자만 접근 가능합니다.")
    @PutMapping("/{id}")
    fun updateSeries(
        @PathVariable id: Long,
        @RequestBody @Valid request: SeriesRequest
    ): ResponseEntity<ResponseDto<SeriesDetailResponse>> {
        val response = seriesService.updateSeries(id, request)
        return ResponseEntity.ok(ResponseDto.ok(response))
    }

    @Operation(summary = "시리즈 삭제", description = "시리즈를 삭제합니다. 관리자만 접근 가능합니다.")
    @DeleteMapping("/{id}")
    fun deleteSeries(@PathVariable id: Long): ResponseEntity<ResponseDto<Void>> {
        seriesService.deleteSeries(id)
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(ResponseDto.of(HttpStatus.NO_CONTENT.value().toString(), HttpStatus.NO_CONTENT.reasonPhrase, null))
    }
}
