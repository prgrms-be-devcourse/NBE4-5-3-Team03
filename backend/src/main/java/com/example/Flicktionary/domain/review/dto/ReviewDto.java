package com.example.Flicktionary.domain.review.dto;

import com.example.Flicktionary.domain.review.entity.Review;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class ReviewDto {

    @NonNull
    private Long user_id;

    private Long movie_id;
    private Long series_id;

    @NonNull
    private int rating;
    @NotBlank
    private String comment;

    public ReviewDto(Review review) {

    }
}
