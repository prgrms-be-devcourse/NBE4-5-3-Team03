package com.example.Flicktionary.domain.post.service

import com.example.Flicktionary.domain.post.dto.PostCreateRequestDto
import com.example.Flicktionary.domain.post.dto.PostResponseDto
import com.example.Flicktionary.domain.post.dto.PostResponseDto.Companion.fromEntity
import com.example.Flicktionary.domain.post.dto.PostUpdateRequestDto
import com.example.Flicktionary.domain.post.entity.Post
import com.example.Flicktionary.domain.post.repository.PostRepository
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
class PostService(
    private val postRepository: PostRepository,
    private val userAccountRepository: UserAccountRepository
) {

    // 게시글 생성
    fun create(postDto: PostCreateRequestDto): PostResponseDto {
        // 유저 정보 조회
        val userAccount = userAccountRepository.findById(postDto.userAccountId!!)
            .orElseThrow {
                ServiceException(
                    HttpStatus.NOT_FOUND.value(),
                    "${postDto.userAccountId}번 유저를 찾을 수 없습니다."
                )
            }

        // postDto를 엔티티로 반환
        val post = postDto.toEntity(userAccount)

        // 엔티티 DB에 저장
        val savedPost = postRepository.save(post)

        // 응답 Dto 생성 및 반환
        return fromEntity(savedPost)
    }

    // 게시글 id로 찾기
    fun findById(id: Long): PostResponseDto {
        val post = postRepository.findById(id)
            .orElseThrow {
                ServiceException(
                    HttpStatus.NOT_FOUND.value(),
                    "해당 ID의 게시글을 찾을 수 없습니다."
                )
            }

        return fromEntity(post)
    }

    // 게시글 목록 조회 및 게시글 검색
    fun getPostList(page: Int, pageSize: Int, keyword: String?, keywordType: String?): PageDto<PostResponseDto> {
        // Pageable 객체 생성
        val pageable: Pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        var posts: Page<Post> = postRepository.findAll(pageable)

        // 키워드 타입에 따라 게시글 검색. 없으면 모든 게시글 출력
        if (!keyword.isNullOrBlank() && !keywordType.isNullOrBlank()) {
            posts = when (keywordType) {
                "title" -> postRepository.findByTitleContaining(keyword, pageable)
                "content" -> postRepository.findByContentContaining(keyword, pageable)
                "nickname" -> postRepository.findByUserAccount_NicknameContaining(keyword, pageable)
                else -> throw ServiceException(
                    HttpStatus.BAD_REQUEST.value(),
                    "지원하지 않는 검색 타입입니다: $keywordType"
                )
            }
        } else {
            postRepository.findAll(pageable)
        }

        // PageDto<PostResponseDto>로 변환
        val postDtoPage = posts.map { fromEntity(it) }

        // PageDto 반환
        return PageDto(postDtoPage)
    }

    // 게시글 수정
    fun update(id: Long, postDto: PostUpdateRequestDto): PostResponseDto {
        // 게시글 Id로 해당 게시글 찾기
        val post = postRepository.findById(id)
            .orElseThrow {
                ServiceException(
                    HttpStatus.NOT_FOUND.value(),
                    "해당 게시글을 찾을 수 없습니다."
                )
            }

        // 게시글 제목 수정
        postDto.title?.takeIf { it.isNotBlank() }?.let { post.title = it }

        // 게시글 내용 수정
        postDto.content?.takeIf { it.isNotBlank() }?.let { post.content = it }

        // 게시글 스포일러 여부 수정
        postDto.isSpoiler?.let { post.isSpoiler = it }

        val updatedPost = postRepository.save(post)

        return fromEntity(updatedPost)
    }

    // 게시글 삭제
    fun delete(id: Long) {
        // 게시글 Id로 해당 게시글 찾기
        val post = postRepository.findById(id)
            .orElseThrow {
                ServiceException(
                    HttpStatus.NOT_FOUND.value(),
                    "해당 게시글을 찾을 수 없습니다."
                )
            }

        postRepository.delete(post)
    }
}
