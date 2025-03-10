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
    if (!content.trim()) return alert("리뷰를 입력해주세요!");

    setLoading(true);

    const response = await fetch(`/api/reviews`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ movieId, content, rating }),
    });

    if (!response.ok) {
      alert("리뷰 작성에 실패했습니다.");
      setLoading(false);
      return;
    }

    const newReview = await response.json();
    onReviewAdded(newReview); // 최신 리뷰 추가
    setContent("");
    setRating(5);
    setLoading(false);
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
