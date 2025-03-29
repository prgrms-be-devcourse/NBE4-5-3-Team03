"use client";

import { useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { components } from "@/lib/backend/apiV1/schema";

export default function ClientPage({
  data,
  keyword: initialKeyword,
  keywordType: initialKeywordType,
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
  const searchParams = useSearchParams();
  const [searchKeyword, setSearchKeyword] = useState(initialKeyword);
  const [searchType, setSearchType] = useState(initialKeywordType || "title");
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

  // 게시글 작성 시간
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const formatter = new Intl.DateTimeFormat("ko-KR", {
      year: "numeric",
      month: "numeric",
      day: "numeric",
      hour: "numeric",
      minute: "numeric",
      second: "numeric",
      hour12: true,
    });
    let formattedDate = formatter.format(date);
    // "오전 ", "오후 ", "밤 ", "새벽 " 등의 불필요한 접두사 제거
    formattedDate = formattedDate
      .replace("오전 ", "")
      .replace("오후 ", "")
      .replace("밤 ", "")
      .replace("새벽 ", "");
    return formattedDate;
  };

  // 검색 기능
  const handleSearch = () => {
    router.push(
      `/community?page=1&pageSize=${pageSize}&keyword=${searchKeyword}&keywordType=${searchType}`
    );
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
                className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider"
                style={{
                  width: "10%",
                  borderRight: "1px solid #e5e7eb",
                  borderLeft: "1px solid #e5e7eb",
                  fontSize: "1.1rem",
                  fontWeight: "bold",
                }}
              >
                유저
              </th>
              <th
                scope="col"
                className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider"
                style={{
                  width: "70%",
                  borderRight: "1px solid #e5e7eb",
                  fontSize: "1.1rem",
                  fontWeight: "bold",
                }}
              >
                제목
              </th>
              <th
                scope="col"
                className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider"
                style={{
                  width: "15%",
                  borderRight: "1px solid #e5e7eb",
                  fontSize: "1.1rem",
                  fontWeight: "bold",
                }}
              >
                작성일
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {posts.map((post) => (
              <tr key={post.id} style={post.isSpoiler ? { opacity: 0.1 } : {}}>
                <td
                  className="px-6 py-3 whitespace-nowrap text-sm text-gray-500 text-center"
                  style={{
                    borderRight: "1px solid #e5e7eb",
                    borderBottom: "1px solid #e5e7eb",
                    borderLeft: "1px solid #e5e7eb",
                  }}
                >
                  {post.nickname || "알 수 없음"}
                </td>
                <td
                  className="px-6 py-3 whitespace-nowrap text-sm"
                  style={{
                    borderRight: "1px solid #e5e7eb",
                    borderBottom: "1px solid #e5e7eb",
                  }}
                >
                  <Link
                    href={`/community/${post.id}`}
                    className="hover:underline"
                  >
                    {post.isSpoiler && "(스포)"} {post.title}
                  </Link>
                </td>
                <td
                  className="px-6 py-3 whitespace-nowrap text-sm text-gray-500"
                  style={{
                    borderRight: "1px solid #e5e7eb",
                    borderBottom: "1px solid #e5e7eb",
                  }}
                >
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

      {/* 검색창 */}
      <div className="flex justify-start items-center mt-4">
        <select
          value={searchType}
          onChange={(e) => setSearchType(e.target.value)}
          className="border rounded px-2 py-1 mr-2"
        >
          <option value="title">제목</option>
          <option value="content">내용</option>
          <option value="nickname">유저</option>
        </select>
        <input
          type="text"
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
          onKeyDown={(event) => {
            if (event.key === "Enter") {
              handleSearch();
            }
          }}
          placeholder="검색어를 입력하세요"
          className="border rounded px-2 py-1 mr-2"
        />
        <Button onClick={handleSearch}>검색</Button>
      </div>

      {/* 페이지네이션 */}
      {totalPages > 1 && (
        <div className="flex justify-center mt-8 space-x-2">
          {!isFirstGroup && (
            <Link
              href={`/community?page=${prevPage}&pageSize=${pageSize}&keyword=${searchKeyword}&keywordType=${searchType}`}
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
              href={`/community?page=${pageNo}&pageSize=${pageSize}&keyword=${searchKeyword}&keywordType=${searchType}`}
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
              href={`/community?page=${nextPage}&pageSize=${pageSize}&keyword=${searchKeyword}&keywordType=${searchType}`}
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
          <Button>게시글 등록</Button>
        </Link>
      </div>
    </div>
  );
}
