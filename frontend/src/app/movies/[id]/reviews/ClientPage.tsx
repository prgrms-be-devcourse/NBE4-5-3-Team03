"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Card, CardContent } from "@/components/ui/card";
import { components } from "@/lib/backend/apiV1/schema";
import ReviewForm from "./ReviewForm";

export default function ClientPage({
  data,
  movieId,
}: {
  data: components["schemas"]["PageDtoReviewDto"];
  movieId: number;
}) {
  const router = useRouter();
  const [reviews, setReviews] = useState(data.items);

  // 리뷰 추가 후 리스트 업데이트
  const handleReviewAdded = (
    newReview: components["schemas"]["PageDtoReviewDto"]
  ) => {
    // 최신 리뷰를 맨 위로 추가
    setReviews([newReview, ...reviews]);
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-4">🎬 영화 리뷰</h1>

      {/* 리뷰 작성 폼 */}
      <ReviewForm movieId={movieId} onReviewAdded={handleReviewAdded} />

      {/* 리뷰 목록 */}
      <div className="space-y-4">
        {reviews.map((review) => (
          <Card key={review.id}>
            <CardContent>
              <p>{review.content}</p>
              <p>⭐ {review.rating}</p>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
