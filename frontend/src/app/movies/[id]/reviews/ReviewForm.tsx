"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";

export default function ReviewForm({
  movieId,
  onReviewAdded,
}: {
  movieId: number;
  onReviewAdded: (review: any) => void;
}) {
  const router = useRouter();
  const [content, setContent] = useState("");
  const [rating, setRating] = useState(5);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!content.trim()) return alert("리뷰를 입력해주세요.");

    setLoading(true);

    /* 나중에 로그인 구현되면, 이 곳에 userAccountId를 받아 로그인 하는 기능 구현할 것 */

    const reviewData = {
      userAccountId: 1, // 임시로 1로 설정
      movieId: Number(movieId),
      content,
      rating,
    };

    // 디버깅용 콘솔 로그
    console.log("📡 전송할 리뷰 데이터:", reviewData);

    try {
      const response = await fetch("http://localhost:8080/api/reviews", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(reviewData),
      });

      // 응답 확인
      console.log("서버 응답 상태:", response.status);

      if (!response.ok) {
        alert("리뷰 작성에 실패했습니다.");
        setLoading(false);

        // 로그
        const errorData = await response.json();
        console.error("리뷰 작성 실패:", errorData);
        alert(`리뷰 작성 실패: ${errorData.message || response.statusText}`);
        return;
      }

      const newReview = await response.json();

      console.log("작성된 리뷰:", newReview);

      // 최신 리뷰 추가
      onReviewAdded(newReview);
      setContent("");
      setRating(5);
      setLoading(false);
    } catch (error) {
      console.error("네트워크 오류:", error);
      alert("리뷰 작성 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mb-6">
      <Input
        type="text"
        placeholder="리뷰를 입력하세요"
        value={content}
        onChange={(e) => setContent(e.target.value)}
      />
      <Button onClick={handleSubmit} disabled={loading}>
        {loading ? "등록 중..." : "리뷰 작성"}
      </Button>
    </div>
  );
}
