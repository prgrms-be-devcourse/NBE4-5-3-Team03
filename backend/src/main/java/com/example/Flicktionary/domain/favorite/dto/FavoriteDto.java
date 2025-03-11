package com.example.Flicktionary.domain.favorite.dto;

import com.example.Flicktionary.domain.favorite.entity.ContentType;
import com.example.Flicktionary.domain.favorite.entity.Favorite;
import com.example.Flicktionary.domain.user.entity.UserAccount;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteDto {
    private Long id;
    private Long userId;
    private ContentType contentType;
    private Long contentId;
    private Object data;

    // Entity -> DTO
    public static FavoriteDto fromEntity(Favorite favorite) {
        Object contentData = null;
        if (favorite.getContentType() == ContentType.MOVIE && favorite.getMovie() != null) {
            contentData = FavoriteContentDto.fromMovie(favorite.getMovie());
        } else if (favorite.getContentType() == ContentType.SERIES && favorite.getSeries() != null) {
            contentData = FavoriteContentDto.fromSeries(favorite.getSeries());
        }

        return FavoriteDto.builder()
                .id(favorite.getId())
                .userId(favorite.getUserAccount().getId())
                .contentType(favorite.getContentType())
                .contentId(favorite.getContentId())
                .data(contentData)
                .build();
    }

    // DTO -> Entity
    public Favorite toEntity(UserAccount user) {
        return Favorite.builder()
                .userAccount(user)
                .contentType(this.contentType)
                .contentId(this.contentId)
                .build();
    }
}
