package com.example.Flicktionary.domain.post.controller;

import com.example.Flicktionary.domain.post.dto.PostCreateRequestDto;
import com.example.Flicktionary.domain.post.dto.PostResponseDto;
import com.example.Flicktionary.domain.post.dto.PostUpdateRequestDto;
import com.example.Flicktionary.domain.post.service.PostService;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.entity.UserAccountType;
import com.example.Flicktionary.domain.user.service.UserAccountJwtAuthenticationService;
import com.example.Flicktionary.domain.user.service.UserAccountService;
import com.example.Flicktionary.global.dto.PageDto;
import com.example.Flicktionary.global.exception.ServiceException;
import com.example.Flicktionary.global.security.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("게시글 컨트롤러 테스트")
@Import({PostService.class,
        UserAccountService.class,
        UserAccountJwtAuthenticationService.class,
        CustomUserDetailsService.class})
@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private UserAccountService userAccountService;

    @MockitoBean
    private UserAccountJwtAuthenticationService userAccountJwtAuthenticationService;

    /* 테스트용 변수 설정 */
    // 테스트용 유저
    private final UserAccount testUser = new UserAccount(
            1L,
            "테스트용 유저",
            "test12345",
            "test@email.com",
            "테스트 유저",
            UserAccountType.USER);

    // 검증용 게시글 응답 Dto
    private final PostResponseDto expectedPost = PostResponseDto.builder()
            .id(1L)
            .userAccountId(testUser.getId())
            .nickname("테스트 유저")
            .title("테스트 제목")
            .content("테스트 내용")
            .isSpoiler(false)
            .build();

    @Test
    @DisplayName("게시글 생성")
    void createPost() throws Exception {

        // 테스트용 게시글 생성
        PostCreateRequestDto postDto = PostCreateRequestDto.builder()
                .userAccountId(testUser.getId())
                .title("테스트 제목")
                .content("테스트 내용")
                .isSpoiler(false)
                .build();

        // 메서드 실행
        given(postService.create(any(PostCreateRequestDto.class))).willReturn(expectedPost);

        // mockMvc로 post 요청 후 Content-Type 설정과 요청 본문 설정
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.nickname").value(expectedPost.getNickname()))
                .andExpect(jsonPath("$.data.title").value(expectedPost.getTitle()))
                .andExpect(jsonPath("$.data.content").value(expectedPost.getContent()));

        then(postService).should().create(any(PostCreateRequestDto.class));
    }

    @Test
    @DisplayName("특정 게시글 조회 - 성공")
    void getPostByIdSuccess() throws Exception {

        // 게시글의 Id로 게시글을 찾아 반환
        Long postId = 1L;
        given(postService.findById(postId)).willReturn(expectedPost);

        // mockMvc로 get 요청 후 검증
        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(expectedPost.getId()))
                .andExpect(jsonPath("$.data.title").value(expectedPost.getTitle()))
                .andExpect(jsonPath("$.data.content").value(expectedPost.getContent()));

        then(postService).should().findById(postId);
    }

    @Test
    @DisplayName("특정 게시글 조회 - 실패")
    void getPostByIdFailure() throws Exception {

        // 존재하지 않는 게시글의 id 생성한 뒤 없으면 예외 반환
        Long postId = 1111L;
        given(postService.findById(postId)).willThrow(new ServiceException(HttpStatus.NOT_FOUND.value(), "해당 Id의 게시글을 찾을 수 없습니다."));

        // mockMvc로 get 요청 후 검증
        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.message").value("해당 Id의 게시글을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("게시글 목록 조회")
    void getPostList() throws Exception {

        // 요청할 페이지 번호와 페이지당 게시글 수, 테스트용 게시글 저장
        int page = 1;
        int size = 10;
        List<PostResponseDto> postList = List.of(
                PostResponseDto.builder()
                        .id(2L)
                        .userAccountId(testUser.getId())
                        .nickname("테스트 유저")
                        .title("테스트 제목2")
                        .content("테스트 내용2")
                        .isSpoiler(false)
                        .build(),
                PostResponseDto.builder()
                        .id(3L)
                        .userAccountId(testUser.getId())
                        .nickname("테스트 유저")
                        .title("테스트 제목3")
                        .content("테스트 내용3")
                        .isSpoiler(true)
                        .build()
        );

        PageDto<PostResponseDto> pageDto = new PageDto<>(postList, 2, 1, 1, 2, "createdAt");
        given(postService.getPostList(page, size)).willReturn(pageDto);

        // 로그 확인
        System.out.println(" ========= 게시글 목록 조회 ========= ");
        for (PostResponseDto post : pageDto.getItems()) {
            System.out.println("작성자 닉네임: " + post.getNickname());
            System.out.println("제목: " + post.getTitle());
            System.out.println("내용: " + post.getContent());
            System.out.println("생성 시간: " + post.getCreatedAt());
            System.out.println("스포일러(true면 스포일러, false면 스포일러 아님): " + post.getIsSpoiler());
            System.out.println(" =================================== ");
        }

        // mockMvc로 get 요청 후 검증
        mockMvc.perform(get("/api/posts")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(postList.size()))
                .andExpect(jsonPath("$.data.items[0].id").value(postList.getFirst().getId()))
                .andExpect(jsonPath("$.data.items[0].title").value(postList.getFirst().getTitle()))
                .andExpect(jsonPath("$.data.items[0].content").value(postList.getFirst().getContent()))
                .andExpect(jsonPath("$.data.items[1].id").value(postList.getLast().getId()))
                .andExpect(jsonPath("$.data.items[1].title").value(postList.getLast().getTitle()))
                .andExpect(jsonPath("$.data.items[1].content").value(postList.getLast().getContent()))
                .andExpect(jsonPath("$.data.totalPages").value(pageDto.getTotalPages()))
                .andExpect(jsonPath("$.data.totalItems").value(pageDto.getTotalItems()));

        then(postService).should().getPostList(page, size);
    }

    @Test
    @DisplayName("제목으로 게시글 검색")
    void searchPostsByTitle() throws Exception {

        // 검색어, 요청할 페이지 번호, 페이지당 게시글 수, 테스트용 게시글 저장
        String keyword = "찾을 제목";
        int page = 1;
        int size = 10;
        List<PostResponseDto> postList = List.of(
                PostResponseDto.builder()
                        .id(2L)
                        .userAccountId(testUser.getId())
                        .nickname("찾을 테스트 유저")
                        .title("찾을 제목2")
                        .content("찾을 내용2")
                        .isSpoiler(false)
                        .build(),
                PostResponseDto.builder()
                        .id(3L)
                        .userAccountId(testUser.getId())
                        .nickname("테스트 유저")
                        .title("테스트 제목3")
                        .content("테스트 내용3")
                        .isSpoiler(true)
                        .build()
        );

        List<PostResponseDto> searchPosts = List.of(postList.getFirst());

        PageDto<PostResponseDto> pageDto = new PageDto<>(searchPosts, 1, 1, 1, 1, "createdAt");
        given(postService.searchPostsByTitle(keyword, page, size)).willReturn(pageDto);

        // 로그 확인
        System.out.println(" ========= 게시글 목록 조회 ========= ");
        for (PostResponseDto post : pageDto.getItems()) {
            System.out.println("작성자 닉네임: " + post.getNickname());
            System.out.println("제목: " + post.getTitle());
            System.out.println("내용: " + post.getContent());
            System.out.println("생성 시간: " + post.getCreatedAt());
            System.out.println("스포일러(true면 스포일러, false면 스포일러 아님): " + post.getIsSpoiler());
            System.out.println(" =================================== ");
        }

        // mockMvc로 get 요청 후 검증
        mockMvc.perform(get("/api/posts/search/title")
                        .param("title", keyword)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(postList.getFirst().getId()))
                .andExpect(jsonPath("$.data.items[0].title").value(postList.getFirst().getTitle()))
                .andExpect(jsonPath("$.data.items[0].content").value(postList.getFirst().getContent()))
                .andExpect(jsonPath("$.data.totalPages").value(pageDto.getTotalPages()))
                .andExpect(jsonPath("$.data.totalItems").value(pageDto.getTotalItems()));

        then(postService).should().searchPostsByTitle(keyword, page, size);
    }

    @Test
    @DisplayName("내용으로 게시글 검색")
    void searchPostsByContent() throws Exception {

        // 검색어, 요청할 페이지 번호, 페이지당 게시글 수, 테스트용 게시글 저장
        String keyword = "찾을 내용";
        int page = 1;
        int size = 10;
        List<PostResponseDto> postList = List.of(
                PostResponseDto.builder()
                        .id(2L)
                        .userAccountId(testUser.getId())
                        .nickname("찾을 테스트 유저")
                        .title("찾을 제목2")
                        .content("찾을 내용2")
                        .isSpoiler(false)
                        .build(),
                PostResponseDto.builder()
                        .id(3L)
                        .userAccountId(testUser.getId())
                        .nickname("테스트 유저")
                        .title("테스트 제목3")
                        .content("테스트 내용3")
                        .isSpoiler(true)
                        .build()
        );

        List<PostResponseDto> searchPosts = List.of(postList.getFirst());

        PageDto<PostResponseDto> pageDto = new PageDto<>(searchPosts, 1, 1, 1, 1, "createdAt");
        given(postService.searchPostsByContent(keyword, page, size)).willReturn(pageDto);

        // 로그 확인
        System.out.println(" ========= 게시글 목록 조회 ========= ");
        for (PostResponseDto post : pageDto.getItems()) {
            System.out.println("작성자 닉네임: " + post.getNickname());
            System.out.println("제목: " + post.getTitle());
            System.out.println("내용: " + post.getContent());
            System.out.println("생성 시간: " + post.getCreatedAt());
            System.out.println("스포일러(true면 스포일러, false면 스포일러 아님): " + post.getIsSpoiler());
            System.out.println(" =================================== ");
        }

        // mockMvc로 get 요청 후 검증
        mockMvc.perform(get("/api/posts/search/content")
                        .param("content", keyword)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(postList.getFirst().getId()))
                .andExpect(jsonPath("$.data.items[0].title").value(postList.getFirst().getTitle()))
                .andExpect(jsonPath("$.data.items[0].content").value(postList.getFirst().getContent()))
                .andExpect(jsonPath("$.data.totalPages").value(pageDto.getTotalPages()))
                .andExpect(jsonPath("$.data.totalItems").value(pageDto.getTotalItems()));

        then(postService).should().searchPostsByContent(keyword, page, size);
    }

    @Test
    @DisplayName("유저 닉네임으로 게시글 검색")
    void searchPostsByNickname() throws Exception {

        // 검색어, 요청할 페이지 번호, 페이지당 게시글 수, 테스트용 게시글 저장
        String keyword = "찾을 유저";
        int page = 1;
        int size = 10;
        List<PostResponseDto> postList = List.of(
                PostResponseDto.builder()
                        .id(2L)
                        .userAccountId(testUser.getId())
                        .nickname("찾을 유저")
                        .title("찾을 제목2")
                        .content("찾을 내용2")
                        .isSpoiler(false)
                        .build(),
                PostResponseDto.builder()
                        .id(3L)
                        .userAccountId(testUser.getId())
                        .nickname("테스트 유저")
                        .title("테스트 제목3")
                        .content("테스트 내용3")
                        .isSpoiler(true)
                        .build()
        );

        List<PostResponseDto> searchPosts = List.of(postList.getFirst());

        PageDto<PostResponseDto> pageDto = new PageDto<>(searchPosts, 1, 1, 1, 1, "createdAt");
        given(postService.searchPostsByNickname(keyword, page, size)).willReturn(pageDto);

        // 로그 확인
        System.out.println(" ========= 게시글 목록 조회 ========= ");
        for (PostResponseDto post : pageDto.getItems()) {
            System.out.println("작성자 닉네임: " + post.getNickname());
            System.out.println("제목: " + post.getTitle());
            System.out.println("내용: " + post.getContent());
            System.out.println("생성 시간: " + post.getCreatedAt());
            System.out.println("스포일러(true면 스포일러, false면 스포일러 아님): " + post.getIsSpoiler());
            System.out.println(" =================================== ");
        }

        // mockMvc로 get 요청 후 검증
        mockMvc.perform(get("/api/posts/search/nickname")
                        .param("nickname", keyword)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(postList.getFirst().getId()))
                .andExpect(jsonPath("$.data.items[0].title").value(postList.getFirst().getTitle()))
                .andExpect(jsonPath("$.data.items[0].content").value(postList.getFirst().getContent()))
                .andExpect(jsonPath("$.data.totalPages").value(pageDto.getTotalPages()))
                .andExpect(jsonPath("$.data.totalItems").value(pageDto.getTotalItems()));

        then(postService).should().searchPostsByNickname(keyword, page, size);
    }

    @Test
    @DisplayName("게시글 수정")
    void postUpdate() throws Exception {

        // 수정할 dto와 검증 dto 저장
        Long postId = 1L;
        PostUpdateRequestDto updateRequestDto = PostUpdateRequestDto.builder()
                .title("수정된 테스트 제목")
                .content("수정된 테스트 내용")
                .isSpoiler(true)
                .build();

        PostResponseDto updatedPostResponseDto = PostResponseDto.builder()
                .id(postId)
                .userAccountId(testUser.getId())
                .nickname(testUser.getNickname())
                .title(updateRequestDto.getTitle())
                .content(updateRequestDto.getContent())
                .isSpoiler(updateRequestDto.getIsSpoiler())
                .build();

        given(postService.update(postId, updateRequestDto)).willReturn(updatedPostResponseDto);

        // mockMvc로 put 요청 후 검증
        mockMvc.perform(put("/api/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(updatedPostResponseDto.getId()))
                .andExpect(jsonPath("$.data.userAccountId").value(updatedPostResponseDto.getUserAccountId()))
                .andExpect(jsonPath("$.data.nickname").value(updatedPostResponseDto.getNickname()))
                .andExpect(jsonPath("$.data.title").value(updateRequestDto.getTitle()))
                .andExpect(jsonPath("$.data.content").value(updateRequestDto.getContent()))
                .andExpect(jsonPath("$.data.isSpoiler").value(updateRequestDto.getIsSpoiler()));

        then(postService).should().update(postId, updateRequestDto);
    }

    @Test
    @DisplayName("게시글 삭제")
    void postDelete() throws Exception {

        // 삭제 메서드 실행
        Long postId = 1L;
        willDoNothing().given(postService).delete(postId);

        // mockMvc로 delete 요청 후 검증
        mockMvc.perform(delete("/api/posts/{id}", postId))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.code").value(String.valueOf(HttpStatus.NO_CONTENT.value())))
                .andExpect(jsonPath("$.message").value(HttpStatus.NO_CONTENT.getReasonPhrase()))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(postService).should().delete(postId);
    }
}
