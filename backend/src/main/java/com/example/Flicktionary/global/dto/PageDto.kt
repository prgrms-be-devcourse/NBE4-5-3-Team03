package com.example.Flicktionary.global.dto

import org.springframework.data.domain.Page
import org.springframework.lang.NonNull

data class PageDto<T>(
    @NonNull
    val items: List<T>,

    @NonNull
    val totalPages: Int,

    @NonNull
    val totalItems: Int,

    @NonNull
    val curPageNo: Int,

    @NonNull
    val pageSize: Int,

    @NonNull
    val sortBy: String
) {
    constructor(page: Page<T>) : this(
        items = page.content,
        totalPages = page.totalPages,
        totalItems = page.totalElements.toInt(),
        curPageNo = page.number + 1,
        pageSize = page.size,
        sortBy = page.sort.toString()
    )
}
