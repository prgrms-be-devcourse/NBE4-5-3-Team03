package com.example.Flicktionary.domain.post.controller

import com.example.Flicktionary.domain.post.dto.PostCreateRequestDto
import com.example.Flicktionary.domain.post.dto.PostResponseDto
import com.example.Flicktionary.domain.post.dto.PostUpdateRequestDto
import com.example.Flicktionary.domain.post.service.PostService
import com.example.Flicktionary.domain.user.entity.UserAccount
import com.example.Flicktionary.domain.user.entity.UserAccountType
import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService
import com.example.Flicktionary.domain.user.service.UserAccountService
import com.example.Flicktionary.global.dto.PageDto
import com.example.Flicktionary.global.exception.ServiceException
import com.example.Flicktionary.global.security.CustomUserDetailsService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@DisplayName("게시글 컨트롤러 테스트")
@Import(
    PostService::class,
    UserAccountService::class,
    UserAccountJwtAuthenticationService::class,
    CustomUserDetailsService::class
)
@WebMvcTest(PostController::class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var postService: PostService

    @MockitoBean
    private lateinit var userAccountService: UserAccountService

    @MockitoBean
    private lateinit var userAccountJwtAuthenticationService: UserAccountJwtAuthenticationService

    /* 테스트용 변수 설정 */ // 테스트용 유저
    private val testUser = UserAccount(
        1L,
        "테스트용 유저",
        "test12345",
        "test@email.com",
        "테스트 유저",
        UserAccountType.USER
    )

    // 검증용 게시글 응답 Dto
    private val expectedPost = PostResponseDto(
        id = 1L,
        userAccountId = testUser.id,
        nickname = "테스트 유저",
        title = "테스트 제목",
        content = "테스트 내용",
        isSpoiler = false
    )

    @Test
    @DisplayName("게시글 생성")
    fun createPost() {
        // 테스트용 게시글 생성
        val postDto = PostCreateRequestDto(
            userAccountId = testUser.id,
            title = "테스트 제목",
            content = "테스트 내용",
            isSpoiler = false
        )

        // 메서드 실행
        given(postService.create(postDto)).willReturn(expectedPost)

        // mockMvc로 post 요청 후 Content-Type 설정과 요청 본문 설정
        mockMvc.perform(
            post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.nickname").value(expectedPost.nickname))
            .andExpect(jsonPath("$.data.title").value(expectedPost.title))
            .andExpect(jsonPath("$.data.content").value(expectedPost.content))

        then(postService).should().create(postDto)
    }

    @Test
    @DisplayName("특정 게시글 조회 - 성공")
    fun getPostByIdSuccess() {
        // 게시글의 Id로 게시글을 찾아 반환
        val postId = 1L
        given(postService.findById(postId)).willReturn(expectedPost)

        // mockMvc로 get 요청 후 검증
        mockMvc.perform(get("/api/posts/$postId"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(expectedPost.id))
            .andExpect(jsonPath("$.data.title").value(expectedPost.title))
            .andExpect(jsonPath("$.data.content").value(expectedPost.content))

        then(postService).should().findById(postId)
    }

    @Test
    @DisplayName("특정 게시글 조회 - 실패")
    fun getPostByIdFailure() {
        // 존재하지 않는 게시글의 id 생성한 뒤 없으면 예외 반환
        val postId = 1111L
        given(postService.findById(postId)).willThrow(
            ServiceException(
                HttpStatus.NOT_FOUND.value(),
                "해당 Id의 게시글을 찾을 수 없습니다."
            )
        )

        // mockMvc로 get 요청 후 검증
        mockMvc.perform(get("/api/posts/$postId"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.message").value("해당 Id의 게시글을 찾을 수 없습니다."))
    }

    @Test
    @DisplayName("게시글 목록 조회 및 검색")
    fun getPostListAndSearch() {
        // 페이지 설정
        val page = 1
        val pageSize = 10

        // 테스트용 게시글 목록 생성
        val postList = listOf(
            PostResponseDto(
                id = 2L,
                userAccountId = testUser.id,
                nickname = testUser.nickname,
                title = "테스트 제목2",
                content = "테스트 내용2",
                isSpoiler = false,
                createdAt = null
            ),
            PostResponseDto(
                id = 3L,
                userAccountId = testUser.id,
                nickname = testUser.nickname,
                title = "테스트 제목3",
                content = "테스트 내용3",
                isSpoiler = true,
                createdAt = null
            )
        )

        val pageDto =
            PageDto(postList, 2, 1, 1, 2, "createdAt")

        // 로그 확인
        println(" ========= 게시글 목록 조회 ========= ")
        for (post in pageDto.items) {
            println("작성자 닉네임: " + post.nickname)
            println("제목: " + post.title)
            println("내용: " + post.content)
            println("생성 시간: " + post.createdAt)
            println("스포일러(true면 스포일러, false면 스포일러 아님): " + post.isSpoiler)
            println(" =================================== ")
        }

        // 게시글 전체 조회
        given(
            postService.getPostList(
                page,
                pageSize,
                null,
                null
            )
        ).willReturn(pageDto)
        mockMvc.perform(
            get("/api/posts")
                .param("page", page.toString())
                .param("pageSize", pageSize.toString())
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.items.length()").value(postList.size))
            .andExpect(jsonPath("$.data.totalPages").value(pageDto.totalPages))
            .andExpect(jsonPath("$.data.totalItems").value(pageDto.totalItems))
        then(postService).should().getPostList(page, pageSize, null, null)

        // 제목으로 검색
        val searchTitle = "찾을 제목"
        val titleSearchResult = listOf(postList.first())
        val titleSearchPageDto =
            PageDto(titleSearchResult, 1, 1, 1, 1, "createdAt")
        given(
            postService.getPostList(
                page,
                pageSize,
                searchTitle,
                "title"
            )
        ).willReturn(titleSearchPageDto)
        mockMvc.perform(
            get("/api/posts")
                .param("page", page.toString())
                .param("pageSize", pageSize.toString())
                .param("keyword", searchTitle)
                .param("keywordType", "title")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.items.length()").value(titleSearchResult.size))
            .andExpect(jsonPath("$.data.totalPages").value(titleSearchPageDto.totalPages))
            .andExpect(jsonPath("$.data.totalItems").value(titleSearchPageDto.totalItems))
        then(postService).should().getPostList(page, pageSize, searchTitle, "title")

        // 내용으로 검색
        val searchContent = "찾을 내용"
        val contentSearchResult = listOf(postList.first())
        val contentSearchPageDto =
            PageDto(
                contentSearchResult,
                1,
                1,
                1,
                1,
                "createdAt"
            )
        given(
            postService.getPostList(
                page,
                pageSize,
                searchContent,
                "content"
            )
        ).willReturn(contentSearchPageDto)
        mockMvc.perform(
            get("/api/posts")
                .param("page", page.toString())
                .param("pageSize", pageSize.toString())
                .param("keyword", searchContent)
                .param("keywordType", "content")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.items.length()").value(contentSearchResult.size))
            .andExpect(jsonPath("$.data.totalPages").value(contentSearchPageDto.totalPages))
            .andExpect(jsonPath("$.data.totalItems").value(contentSearchPageDto.totalItems))
        then(postService).should().getPostList(page, pageSize, searchContent, "content")

        // 유저 닉네임으로 검색
        val searchNickname = "찾을 유저"
        val nicknameSearchResult = listOf(postList.first())
        val nicknameSearchPageDto =
            PageDto(
                nicknameSearchResult,
                1,
                1,
                1,
                1,
                "createdAt"
            )
        given(
            postService.getPostList(
                page,
                pageSize,
                searchNickname,
                "nickname"
            )
        ).willReturn(nicknameSearchPageDto)
        mockMvc.perform(
            get("/api/posts")
                .param("page", page.toString())
                .param("pageSize", pageSize.toString())
                .param("keyword", searchNickname)
                .param("keywordType", "nickname")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items").isArray())
            .andExpect(jsonPath("$.data.items.length()").value(nicknameSearchResult.size))
            .andExpect(jsonPath("$.data.totalPages").value(nicknameSearchPageDto.totalPages))
            .andExpect(jsonPath("$.data.totalItems").value(nicknameSearchPageDto.totalItems))
        then(postService).should().getPostList(page, pageSize, searchNickname, "nickname")
    }

    @Test
    @DisplayName("게시글 수정")
    fun postUpdate() {
        // 수정할 dto와 검증 dto 저장
        val postId = 1L
        val updateRequestDto = PostUpdateRequestDto(
            title = "수정된 테스트 제목",
            content = "수정된 테스트 내용",
            isSpoiler = true
        )

        val updatedPostResponseDto = PostResponseDto(
            id = postId,
            userAccountId = testUser.id,
            nickname = testUser.nickname,
            title = updateRequestDto.title!!,
            content = updateRequestDto.content!!,
            isSpoiler = updateRequestDto.isSpoiler
        )

        given(postService.update(postId, updateRequestDto)).willReturn(updatedPostResponseDto)

        // mockMvc로 put 요청 후 검증
        mockMvc.perform(
            put("/api/posts/{id}", postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequestDto))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(updatedPostResponseDto.id))
            .andExpect(
                jsonPath("$.data.userAccountId").value(updatedPostResponseDto.userAccountId)
            )
            .andExpect(jsonPath("$.data.nickname").value(updatedPostResponseDto.nickname))
            .andExpect(jsonPath("$.data.title").value(updateRequestDto.title))
            .andExpect(jsonPath("$.data.content").value(updateRequestDto.content))
            .andExpect(jsonPath("$.data.isSpoiler").value(updateRequestDto.isSpoiler))

        then(postService).should().update(postId, updateRequestDto)
    }

    @Test
    @DisplayName("게시글 삭제")
    fun postDelete() {
        // 삭제 메서드 실행
        val postId = 1L
        willDoNothing().given(postService).delete(postId)

        // mockMvc로 delete 요청 후 검증
        mockMvc.perform(delete("/api/posts/{id}", postId))
            .andExpect(status().isNoContent())
            .andExpect(jsonPath("$.code").value(HttpStatus.NO_CONTENT.value().toString()))
            .andExpect(jsonPath("$.message").value(HttpStatus.NO_CONTENT.reasonPhrase))
            .andExpect(jsonPath("$.data").doesNotExist())

        then(postService).should().delete(postId)
    }
}
