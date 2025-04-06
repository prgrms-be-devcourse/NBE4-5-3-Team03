package com.example.Flicktionary.domain.post.controller

import com.example.Flicktionary.domain.post.dto.PostCreateRequestDto
import com.example.Flicktionary.domain.post.dto.PostResponseDto
import com.example.Flicktionary.domain.post.dto.PostUpdateRequestDto
import com.example.Flicktionary.domain.post.service.PostService
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.dto.ResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postService: PostService
) {

    // 게시글 생성
    @PostMapping
    fun create(
        @RequestBody postDto: PostCreateRequestDto
    ): ResponseEntity<ResponseDto<PostResponseDto>> {
        val post = postService.create(postDto)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ResponseDto.of(HttpStatus.CREATED.value().toString() + "", HttpStatus.CREATED.reasonPhrase, post))
    }

    // 특정 게시글 조회
    @GetMapping("/{id}")
    fun getPostById(
        @PathVariable id: Long
    ): ResponseEntity<ResponseDto<PostResponseDto>> {
        val post = postService.findById(id)
        return ResponseEntity.ok(ResponseDto.ok(post))
    }

    // 게시글 목록 조회 및 게시글 검색
    @GetMapping
    fun getPostList(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(required = false) keywordType: String?
    ): ResponseEntity<ResponseDto<PageDto<PostResponseDto>>> {
        val postList = postService.getPostList(page, pageSize, keyword, keywordType)
        return ResponseEntity.ok(ResponseDto.ok(postList))
    }

    // 게시글 수정
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody postDto: PostUpdateRequestDto
    ): ResponseEntity<ResponseDto<PostResponseDto>> {
        val post = postService.update(id, postDto)
        return ResponseEntity.ok(ResponseDto.ok(post))
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long
    ): ResponseEntity<ResponseDto<*>> {
        postService.delete(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(ResponseDto.of(HttpStatus.NO_CONTENT.value().toString() + "", HttpStatus.NO_CONTENT.reasonPhrase))
    }
}
