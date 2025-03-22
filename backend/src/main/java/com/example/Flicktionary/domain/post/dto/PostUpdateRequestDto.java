package com.example.Flicktionary.domain.post.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostUpdateRequestDto {
    private Long id;
    private String title;
    private String content;
    private Boolean isSpoiler;
}
