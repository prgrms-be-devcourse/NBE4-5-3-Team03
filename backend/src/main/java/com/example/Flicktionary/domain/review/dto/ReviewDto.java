package com.example.Flicktionary.domain.review.dto;

import com.example.Flicktionary.domain.review.entity.Review;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {

    @NonNull
    private Long id;

    @NonNull
    private Long user_id;

    private Long movie_id;
    private Long series_id;

    @NonNull
    private int rating;
    @NotBlank
    private String content;

    // Entity를 DTO로 변환
    public static ReviewDto fromEntity(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .user_id(review.getUser().getId())
                .movie_id(review.getMovie() != null ? review.getMovie().getId() : null)
                .series_id(review.getSeries() != null ? review.getSeries().getId() : null)
                .rating(review.getRating())
                .content(review.getContent())
                .build();
    }

    // DTO를 Entity로 변환
    public Review toEntity() {
        return Review.builder()
                .id(this.id)
                .rating(this.rating)
                .content(this.content)
                .build();
    }
}
