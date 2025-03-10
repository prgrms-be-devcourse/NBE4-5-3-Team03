"use client";

import { useEffect, useState } from "react";
import client from "@/lib/backend/client";
import { components } from "@/lib/backend/apiV1/schema";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableHead,
  TableHeader,
  TableRow,
  TableCell,
} from "/src/components/ui/table";

export default function AdminReviewPage() {
  const router = useRouter();
  const [reviews, setReviews] = useState<components["schemas"]["ReviewDto"][]>(
    []
  );
  const [loading, setLoading] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [sortBy, setSortBy] = useState("latest"); // 기본 정렬: 최신 순

  // 리뷰 목록 가져오기
  const fetchReviews = async () => {
    setLoading(true);
    const response = await client.GET("/api/reviews", {});

    if (!response.error) {
      setReviews(response.data || []);
    }
    setLoading(false);
  };

  // 삭제 기능
  const handleDelete = async (reviewId: number) => {
    if (!confirm("정말 삭제하시겠습니까?")) return;

    const response = await client.DELETE("/api/reviews/{id}" as any, {
      params: { path: { id: reviewId } },
    });

    if (!response.error) {
      alert("삭제되었습니다.");
      fetchReviews(); // 삭제 후 목록 갱신
    } else {
      alert("삭제 실패: " + response.error.message);
    }
  };

  useEffect(() => {
    fetchReviews();
  }, []);

  return (
    <div className="max-w-5xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-4">리뷰 관리</h1>

      {/* 검색 & 정렬 */}
      <div className="flex gap-3 mb-4">
        <Input
          placeholder="닉네임 또는 내용 검색"
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
        />
        <Button onClick={fetchReviews}>검색</Button>
      </div>

      {/* 리뷰 목록 */}
      {loading ? (
        <p>로딩 중...</p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>닉네임</TableHead>
              <TableHead>평점</TableHead>
              <TableHead>내용</TableHead>
              <TableHead>삭제</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {reviews.length > 0 ? (
              reviews.map((review) => (
                <TableRow key={review.id}>
                  <TableCell>{review.nickname}</TableCell>
                  <TableCell>{review.rating} ⭐</TableCell>
                  <TableCell className="truncate w-60">
                    {review.content}
                  </TableCell>
                  <TableCell>
                    <Button
                      variant="destructive"
                      onClick={() => {
                        if (review.id !== undefined) {
                          handleDelete(review.id);
                        } else {
                          console.error("리뷰 id가 존재하지 않습니다.");
                        }
                      }}
                    >
                      삭제
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={4} className="text-center">
                  리뷰가 없습니다.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
