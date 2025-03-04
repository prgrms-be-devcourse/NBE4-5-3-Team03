package com.example.Flicktionary.domain.favorite.entity;

import com.example.Flicktionary.domain.user.entity.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteDto {
    private Long userId;
    private ContentType contentType;
    private Long contentId;

    // Entity -> DTO
    public static FavoriteDto fromEntity(Favorite favorite) {
        return FavoriteDto.builder()
                .userId(favorite.getUser().getId())
                .contentType(favorite.getContentType())
                .contentId(favorite.getContentId())
                .build();
    }

    // DTO -> Entity
    public Favorite toEntity(User user) {
        return Favorite.builder()
                .user(user)
                .contentType(this.contentType)
                .contentId(this.contentId)
                .build();
    }
}
