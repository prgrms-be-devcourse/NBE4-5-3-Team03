package com.example.Flicktionary.domain.post.service;

import com.example.Flicktionary.domain.post.dto.PostCreateRequestDto;
import com.example.Flicktionary.domain.post.dto.PostResponseDto;
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
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));

        // 페이징된 게시글 목록을 Page<Post> 형태로 조회한 뒤 PostResponseDto로 변환
        Page<PostResponseDto> postDtoPage = postRepository.findAll(pageable)
                .map(PostResponseDto::fromEntity);

        // PageDto 반환
        return new PageDto<>(postDtoPage);
    }

    // 제목으로 게시글 검색

    // 작성 유저의 닉네임으로 게시글 검색

    // 게시글 내용으로 게시글 검색

    // 게시글 수정

    // 게시글 삭제

}
