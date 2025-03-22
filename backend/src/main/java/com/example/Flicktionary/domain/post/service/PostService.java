package com.example.Flicktionary.domain.post.service;

import com.example.Flicktionary.domain.post.dto.PostCreateRequestDto;
import com.example.Flicktionary.domain.post.dto.PostResponseDto;
import com.example.Flicktionary.domain.post.dto.PostUpdateRequestDto;
import com.example.Flicktionary.domain.post.entity.Post;
import com.example.Flicktionary.domain.post.repository.PostRepository;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import com.example.Flicktionary.domain.user.repository.UserAccountRepository;
import com.example.Flicktionary.global.dto.PageDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserAccountRepository userAccountRepository;

    // 게시글 생성
    public PostResponseDto create(PostCreateRequestDto postDto) {

        // 유저 정보 조회
        UserAccount userAccount = userAccountRepository.findById(postDto.getUserAccountId())
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));

        // postDto를 엔티티로 반환
        Post post = postDto.toEntity(userAccount);

        // 엔티티 DB에 저장
        Post savedPost = postRepository.save(post);

        // 응답 Dto 생성 및 반환
        return PostResponseDto.fromEntity(savedPost);
    }

    // 게시글 id로 찾기
    public PostResponseDto findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 게시글을 찾을 수 없습니다."));

        return PostResponseDto.fromEntity(post);
    }

    // PageDto로 게시글 목록 조회
    public PageDto<PostResponseDto> getPostList(int page, int size) {

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 페이징된 게시글 목록을 Page<Post> 형태로 조회한 뒤 PostResponseDto로 변환
        Page<PostResponseDto> postDtoPage = postRepository.findAll(pageable)
                .map(PostResponseDto::fromEntity);

        // PageDto 반환
        return new PageDto<>(postDtoPage);
    }

    // 제목으로 게시글 검색
    public PageDto<PostResponseDto> searchPostsByTitle(String title, int page, int size) {

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // PostRepository에서 제목에 키워드를 포함하는 게시글을 페이징하여 조회
        Page<Post> posts = postRepository.findByTitleContaining(title, pageable);

        // PageDto<PostResponseDto>로 변환
        Page<PostResponseDto> postDtoPage = posts.map(PostResponseDto::fromEntity);

        // PageDto 반환
        return new PageDto<>(postDtoPage);
    }

    // 게시글 내용으로 게시글 검색
    public PageDto<PostResponseDto> searchPostsByContent(String content, int page, int size) {

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // PostRepository에서 제목에 키워드를 포함하는 게시글을 페이징하여 조회
        Page<Post> posts = postRepository.findByContentContaining(content, pageable);

        // PageDto<PostResponseDto>로 변환
        Page<PostResponseDto> postDtoPage = posts.map(PostResponseDto::fromEntity);

        // PageDto 반환
        return new PageDto<>(postDtoPage);
    }

    // 작성 유저의 닉네임으로 게시글 검색
    public PageDto<PostResponseDto> searchPostsByNickname(String nickname, int page, int size) {

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // PostRepository에서 제목에 키워드를 포함하는 게시글을 페이징하여 조회
        Page<Post> posts = postRepository.findByUserAccount_NicknameContaining(nickname, pageable);

        // PageDto<PostResponseDto>로 변환
        Page<PostResponseDto> postDtoPage = posts.map(PostResponseDto::fromEntity);

        // PageDto 반환
        return new PageDto<>(postDtoPage);
    }

    // 게시글 수정
    public PostResponseDto update(Long id, PostUpdateRequestDto postDto) {

        // 게시글 Id로 해당 게시글 찾기
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글을 찾을 수 없습니다."));

        // 게시글 제목 수정
        if (postDto.getTitle() != null && !postDto.getTitle().isBlank()) {
            post.setTitle(postDto.getTitle());
        }

        // 게시글 내용 수정
        if (postDto.getContent() != null && !postDto.getContent().isBlank()) {
            post.setContent(postDto.getContent());
        }

        // 게시글 스포일러 여부 수정
        if (postDto.getIsSpoiler() != null) {
            post.setIsSpoiler(postDto.getIsSpoiler());
        }

        // 수정 시간 업데이트
        post.setUpdatedAt(LocalDateTime.now());

        Post updatedPost = postRepository.save(post);

        return PostResponseDto.fromEntity(updatedPost);
    }

    // 게시글 삭제
    public void delete(Long id) {

        // 게시글 Id로 해당 게시글 찾기
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글을 찾을 수 없습니다."));

        postRepository.delete(post);
    }

}
