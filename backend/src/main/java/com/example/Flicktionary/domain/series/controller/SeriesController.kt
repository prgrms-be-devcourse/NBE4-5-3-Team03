package com.example.Flicktionary.domain.series.controller

import com.example.Flicktionary.domain.series.dto.SeriesDetailResponse
import com.example.Flicktionary.domain.series.dto.SeriesSummaryResponse
import com.example.Flicktionary.domain.series.service.SeriesService
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.dto.ResponseDto
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
}
