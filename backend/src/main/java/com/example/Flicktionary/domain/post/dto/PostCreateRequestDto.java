package com.example.Flicktionary.domain.post.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostCreateRequestDto {
    private Long userAccountId;
    private String title;
    private String content;
    private boolean isSpoiler;
}
