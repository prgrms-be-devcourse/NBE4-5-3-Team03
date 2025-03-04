package com.example.Flicktionary;

import com.example.Flicktionary.domain.review.repository.ReviewRepository;
import com.example.Flicktionary.domain.review.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("ReviewTest")
@Transactional
public class ReviewApplicationTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewService reviewService;

    @Test
    @DisplayName("출력 테스트")
    void printHelloWorld() {
        System.out.println("Hello World");
    }

    // 리뷰 작성 테스트

    // 리뷰 삭제 테스트

    // 리뷰 수정 테스트
}
