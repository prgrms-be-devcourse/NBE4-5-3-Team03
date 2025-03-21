package com.example.Flicktionary.domain.post.service;

import com.example.Flicktionary.domain.post.dto.PostCreateRequestDto;
import com.example.Flicktionary.domain.post.dto.PostResponseDto;
import com.example.Flicktionary.domain.post.entity.Post;
import com.example.Flicktionary.domain.post.repository.PostRepository;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.entity.UserAccountType;
import com.example.Flicktionary.domain.user.repository.UserAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("게시글 서비스 테스트")
@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    /* 테스트용 변수 설정 */
    // 테스트용 유저
    private final UserAccount testUser = new UserAccount(
            1L,
            "테스트용 유저",
            "test12345",
            "test@email.com",
            "테스트 유저",
            UserAccountType.USER);

    // 테스트용 요청 게시글 Dto
    private final PostCreateRequestDto requestDto = PostCreateRequestDto.builder()
            .userAccountId(testUser.getId())
            .title("테스트 제목")
            .content("테스트 내용")
            .isSpoiler(false)
            .build();

    // 테스트용 저장된 게시글 엔티티
    private final Post savedPost = Post.builder()
            .id(1L)
            .userAccount(testUser)
            .title("테스트 제목")
            .content("테스트 내용")
            .isSpoiler(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

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
    void createPost() {

        // 유저 Id로 유저를 찾은 뒤 반환 후 저장된 게시글 반환, 테스트 게시글을 생성
        when(userAccountRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);
        PostResponseDto testPostDto = postService.create(requestDto);

        /// 검증
        assertEquals(testPostDto.getId(), expectedPost.getId());
        assertEquals(testPostDto.getUserAccountId(), expectedPost.getUserAccountId());
        assertEquals(testPostDto.getNickname(), expectedPost.getNickname());
        assertEquals(testPostDto.getTitle(), expectedPost.getTitle());
        assertEquals(testPostDto.getContent(), expectedPost.getContent());
        assertEquals(testPostDto.isSpoiler(), expectedPost.isSpoiler());

        // PostRepository의 save 메서드가 한 번 호출되었는지 검증
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 Id로 찾기 - 성공")
    void findPostByIdSuccess() {

        // 저장된 게시글의 id로 특정 게시글을 찾아 반환되도록 설정
        Long postId = savedPost.getId();
        when(postRepository.findById(postId)).thenReturn(Optional.of(savedPost));

        // 게시글 Id로 조회
        PostResponseDto testPostDto = postService.findById(postId);

        /// 검증
        assertEquals(testPostDto.getId(), expectedPost.getId());
        assertEquals(testPostDto.getUserAccountId(), expectedPost.getUserAccountId());
        assertEquals(testPostDto.getNickname(), expectedPost.getNickname());
        assertEquals(testPostDto.getTitle(), expectedPost.getTitle());
        assertEquals(testPostDto.getContent(), expectedPost.getContent());
        assertEquals(testPostDto.isSpoiler(), expectedPost.isSpoiler());

        // PostRepository의 findById 메서드가 호출되었는지 검증
        verify(postRepository).findById(postId);
    }

    @Test
    @DisplayName("게시글 Id로 찾기 - 실패")
    void findPostByIdFailure() {

        // 존재하지 않는 게시글 Id로 findById 호출 시 빈 Optional 반환 설정
        Long postId = 1000L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        /// 검증
        // findPostById 메서드를 호출했을 때 EntityNotFoundException이 발생하는지 확인
        assertThrows(EntityNotFoundException.class, () -> postService.findById(postId));

        // PostRepository의 findById 메서드가 호출되었는지 검증
        verify(postRepository).findById(postId);
    }
}
