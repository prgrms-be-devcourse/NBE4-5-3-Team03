"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { components } from "@/lib/backend/apiV1/schema";

export default function ClientPage({
  data,
  keyword,
  keywordType,
  page,
  pageSize,
}: {
  data: components["schemas"]["PageDtoPostResponseDto"];
  keyword: string;
  keywordType: string;
  page: number;
  pageSize: number;
}) {
  const router = useRouter();
  const posts = data.items;
  const totalPages = data.totalPages;

  // 페이지 그룹 계산 (10개씩 이동)
  const groupSize = 10;
  const currentGroup = Math.floor((page - 1) / groupSize);
  const startPage = currentGroup * groupSize + 1;
  const endPage = Math.min(startPage + groupSize - 1, totalPages);

  // 이전/다음 그룹 이동 페이지 계산
  const prevPage = startPage - 1;
  const nextPage = endPage + 1;

  // 첫 번째 & 마지막 그룹 여부 확인
  const isFirstGroup = startPage === 1;
  const isLastGroup = endPage >= totalPages;

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("ko-KR", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  return (
    <div className="max-w-6xl mx-auto px-4 py-10">
      <h1 className="text-3xl font-bold mb-6 text-center">커뮤니티</h1>
      {/* 게시글 리스트 (테이블 형태) */}
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th
                scope="col"
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                유저
              </th>
              <th
                scope="col"
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                제목
              </th>
              <th
                scope="col"
                className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"
              >
                작성일
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {posts.map((post) => (
              <tr key={post.id}>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {post.nickname || "알 수 없음"}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm">
                  <Link
                    href={`/community/${post.id}`}
                    className="hover:underline"
                  >
                    {post.title}
                  </Link>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {post.createdAt ? formatDate(post.createdAt) : "알 수 없음"}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {posts.length === 0 && (
        <p className="col-span-full text-gray-500 text-lg text-center mt-8">
          등록된 게시글이 없습니다.
        </p>
      )}

      {/* 페이지네이션 */}
      {totalPages > 1 && (
        <div className="flex justify-center mt-8 space-x-2">
          {!isFirstGroup && (
            <Link
              href={`/community?page=${prevPage}&pageSize=${pageSize}&keyword=${keyword}&keywordType=${keywordType}`}
            >
              <Button variant="outline" className="cursor-pointer">
                이전
              </Button>
            </Link>
          )}

          {Array.from(
            { length: endPage - startPage + 1 },
            (_, i) => startPage + i
          ).map((pageNo) => (
            <Link
              key={pageNo}
              href={`/community?page=${pageNo}&pageSize=${pageSize}&keyword=${keyword}&keywordType=${keywordType}`}
            >
              <Button
                variant={pageNo === page ? "default" : "outline"}
                className={`px-4 py-2 cursor-pointer ${
                  pageNo === page
                    ? "font-bold bg-black text-white"
                    : "hover:bg-gray-200"
                }`}
              >
                {pageNo}
              </Button>
            </Link>
          ))}

          {!isLastGroup && (
            <Link
              href={`/community?page=${nextPage}&pageSize=${pageSize}&keyword=${keyword}&keywordType=${keywordType}`}
            >
              <Button variant="outline" className="cursor-pointer">
                다음
              </Button>
            </Link>
          )}
        </div>
      )}
      {/* 등록 버튼 */}
      <div className="fixed bottom-6 right-6">
        <Link href="/community/write">
          <Button>등록</Button>
        </Link>
      </div>
    </div>
  );
}
