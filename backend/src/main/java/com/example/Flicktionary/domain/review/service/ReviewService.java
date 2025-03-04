package com.example.Flicktionary.domain.review.service;

import com.example.Flicktionary.domain.review.dto.ReviewDto;
import com.example.Flicktionary.domain.review.entity.Review;
import com.example.Flicktionary.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    // 리뷰 생성
    public ReviewDto createReview(ReviewDto reviewDto) {

        // ReviewDto를 Entity로 변환해 변수에 저장
        Review review = reviewDto.toEntity();

        // 레포지터리에 DB 영속화 및 변수에 저장
        Review savedReview = reviewRepository.save(review);

        return ReviewDto.fromEntity(savedReview);
    }

    // 모든 리뷰 조회
    public List<ReviewDto> findAllReviews() {

        // findAll()이 Iterable을 반환할 수 없어, List로 변환시켜 변수에 저장
        List<Review> reviews = new ArrayList<>();

        reviewRepository.findAll().forEach(reviews::add);

        // 스트림을 사용해 Entity를 Dto로 변환
        return reviews.stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 리뷰 수정
    public ReviewDto updateReview(Long id, ReviewDto reviewDto) {

        // id로 리뷰를 찾을 수 없을 경우
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 리뷰를 찾을 수 없습니다."));

        // 리뷰의 평점이나 내용 수정
        review.setRating(reviewDto.getRating());
        review.setContent(reviewDto.getContent());

        // DB에 영속화 및 리턴
        Review savedReview = reviewRepository.save(review);
        return ReviewDto.fromEntity(savedReview);
    }

    // 리뷰 삭제
    public void deleteReview(Long id) {

        // id로 리뷰를 찾을 수 없을 경우
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 리뷰를 찾을 수 없습니다."));

        reviewRepository.delete(review);
    }
}
