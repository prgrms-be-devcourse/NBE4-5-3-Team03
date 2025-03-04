package com.example.Flicktionary.domain.review.service;

import com.example.Flicktionary.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

//    @Autowired
//    private UserRepository userRepository;

//    @Autowired
//    private MovieRepository movieRepository;

//    @Autowired
//    private SeriesRepository seriesRepository;

    // 리뷰 엔티티 생성
//    private Review addReview(Review review) {
//        Review createReview = Review.builder()
//                .user(user)
//                .movie(movie)
//                .series(series)
//                .rating(rating)
//                .content(content)
//                .build();
//
//        return reviewRepository.save(createReview);
//    }

    // 리뷰 생성
//    private Review createReview(Review review) {
//
//    }

    // 리뷰 수정
//    private modifyReview() {
//
//    }
}
