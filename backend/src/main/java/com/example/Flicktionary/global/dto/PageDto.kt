package com.example.Flicktionary.global.dto

import org.springframework.data.domain.Page

data class PageDto<T>(
    val items: List<T>,

    val totalPages: Int,

    val totalItems: Int,

    val curPageNo: Int,

    val pageSize: Int,

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