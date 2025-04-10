"use client";

import client from "@/lib/backend/client";
import { components } from "@/lib/backend/apiV1/schema";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { fetchUserProfileClient } from "@/lib/api/user";
import { Button } from "@/components/ui/button";
import Link from "next/link";
import { Input } from "@/components/ui/input";
import {
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Table } from "@/components/ui/table";

export default function AdminCommunityPage() {
  const router = useRouter();
  const [posts, setPosts] = useState<
    components["schemas"]["PostResponseDto"][]
  >([]);
  const [loading, setLoading] = useState(true);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [searchType, setSearchType] = useState("title");
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [isAdmin, setIsAdmin] = useState<boolean | null>(null);

  // 게시글 목록 가져오기
  const fetchAllPosts = async () => {
    const response = await client.GET("/api/posts", {
      params: {
        query: {
          page: page,
          size: pageSize,
          sort: "-createdAt",
        },
      },
    });

    console.log("fetchAllPosts - API 응답 데이터:", response);

    if (!response.error && response.data?.data) {
      setPosts(response.data.data.items || []);
      setTotalPages(Math.ceil((response.data.data.totalItems || 0) / pageSize));
    } else {
      console.warn("fetchAllPosts - 응답 데이터가 없음 (API 확인 필요)");
      setPosts([]);
    }
    setLoading(false);
  };

  // 검색어로 게시글 조회
  const fetchSearchPosts = async () => {
    if (!searchKeyword.trim()) {
      return;
    }
    setLoading(true);

    const response = await client.GET("/api/posts", {
      params: {
        query: {
          keyword: searchKeyword,
          keywordType: searchType,
          page: page,
          size: pageSize,
          sort: "-createdAt",
        },
      },
    });

    if (!response.error && response.data?.data?.items) {
      setPosts(response.data.data.items || []);
      setTotalPages(Math.ceil((response.data.data.totalItems || 0) / pageSize));
    }
    setLoading(false);
  };

  // 삭제 기능
  const handleDelete = async (postId: number) => {
    if (!confirm("정말 삭제하시겠습니까?")) {
      return;
    }

    const response = await client.DELETE("/api/posts/{id}" as any, {
      params: { path: { id: postId } },
    });

    if (!response.error) {
      alert("삭제되었습니다.");

      // 삭제 후 목록 갱신
      fetchAllPosts();
    } else {
      alert("삭제 실패: " + response.error.message);
    }
  };

  useEffect(() => {
    fetchAllPosts();
  }, [page, pageSize]);

  // 검색 버튼 클릭 핸들러
  const handleSearchClick = () => {
    // 검색 시 페이지 1로 초기화
    setPage(1);

    if (searchKeyword.trim() === "") {
      fetchAllPosts();
    } else {
      fetchSearchPosts();
    }
  };

  const handleNextPage = () => {
    if (page < totalPages) {
      setPage(page + 1);
    }
  };

  const handlePreviousPage = () => {
    if (page > 1) {
      setPage(page - 1);
    }
  };

  // 관리자 권한 확인
  useEffect(() => {
    const checkAdmin = async () => {
      const user = await fetchUserProfileClient();

      if (user?.role === "ADMIN") {
        setIsAdmin(true);
      } else {
        setIsAdmin(false);
        alert("관리자 권한이 없습니다.");
        router.push("/");
      }
    };

    checkAdmin();
  }, [router]);

  if (isAdmin === null) {
    return <p>권한 확인 중...</p>;
  }

  if (!isAdmin) {
    return <p>관리자 권한이 없습니다.</p>;
  }

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

    formattedDate = formattedDate
      .replace("오전 ", "")
      .replace("오후 ", "")
      .replace("밤 ", "")
      .replace("새벽 ", "");

    return formattedDate;
  };

  return (
    <div className="max-w-5xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-4">커뮤니티 관리</h1>

      {/* 검색 */}
      <div className="flex gap-3 mb-4">
        <select
          value={searchType}
          onChange={(e) => setSearchType(e.target.value)}
          className="border rounded px-2 py-1 mr-2"
        >
          <option value="title">제목</option>
          <option value="content">내용</option>
          <option value="nickname">유저</option>
        </select>
        <Input
          placeholder="검색어를 입력하세요"
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
        />
        <Button onClick={handleSearchClick}>검색</Button>
      </div>

      {/* 게시글 목록 */}
      {loading ? (
        <p>로딩 중...</p>
      ) : (
        <>
          <Table style={{ tableLayout: "fixed", width: "100%" }}>
            <TableHeader style={{ backgroundColor: "lightgray" }}>
              <TableRow>
                <TableHead
                  style={{
                    borderRight: "1px solid #ccc",
                    fontSize: "1.1rem",
                    fontWeight: "bold",
                    textAlign: "center",
                    color: "black",
                    width: "10%",
                  }}
                >
                  작성자
                </TableHead>
                <TableHead
                  style={{
                    borderRight: "1px solid #ccc",
                    fontSize: "1.1rem",
                    fontWeight: "bold",
                    textAlign: "center",
                    color: "black",
                    width: "10%",
                  }}
                >
                  제목
                </TableHead>
                <TableHead
                  style={{
                    borderRight: "1px solid #ccc",
                    fontSize: "1.1rem",
                    fontWeight: "bold",
                    textAlign: "center",
                    color: "black",
                    width: "auto",
                  }}
                >
                  내용
                </TableHead>
                <TableHead
                  style={{
                    borderRight: "1px solid #ccc",
                    fontSize: "1.1rem",
                    fontWeight: "bold",
                    textAlign: "left",
                    color: "black",
                    width: "15%",
                  }}
                >
                  작성일
                </TableHead>
                <TableHead
                  style={{
                    fontSize: "1.1rem",
                    fontWeight: "bold",
                    textAlign: "center",
                    color: "black",
                    width: "8%",
                  }}
                >
                  삭제
                </TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {posts.length > 0 ? (
                posts.map((post) => (
                  <TableRow key={post.id}>
                    <TableCell
                      style={{
                        borderRight: "1px solid #ccc",
                        textAlign: "center",
                        width: "10%",
                      }}
                    >
                      {post.nickname || "알 수 없음"}
                    </TableCell>
                    <TableCell
                      style={{
                        borderRight: "1px solid #ccc",
                        textAlign: "left",
                        width: "15%",
                        maxWidth: "15%",
                        overflow: "hidden",
                        whiteSpace: "nowrap",
                        textOverflow: "ellipsis",
                      }}
                    >
                      <Link
                        href={`/community/${post.id}`}
                        style={{
                          display: "block",
                          overflow: "hidden",
                          whiteSpace: "nowrap",
                          textOverflow: "ellipsis",
                        }}
                      >
                        {post.title}
                      </Link>
                    </TableCell>
                    <TableCell
                      style={{
                        borderRight: "1px solid #ccc",
                        textAlign: "left",
                        width: "30%",
                        maxWidth: "30%",
                        overflow: "hidden",
                        whiteSpace: "nowrap",
                        textOverflow: "ellipsis",
                      }}
                    >
                      <div
                        style={{
                          display: "block",
                          overflow: "hidden",
                          whiteSpace: "nowrap",
                          textOverflow: "ellipsis",
                        }}
                      >
                        {post.content}
                      </div>
                    </TableCell>
                    <TableCell
                      style={{
                        borderRight: "1px solid #ccc",
                        textAlign: "center",
                        width: "15%",
                      }}
                    >
                      {post.createdAt
                        ? formatDate(post.createdAt)
                        : "알 수 없음"}
                    </TableCell>
                    <TableCell style={{ textAlign: "center", width: "8%" }}>
                      <Button
                        variant="destructive"
                        onClick={() => {
                          if (post.id !== undefined) {
                            handleDelete(post.id);
                          } else {
                            console.error("게시글 id가 존재하지 않습니다.");
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
                  <TableCell colSpan={5} className="text-center">
                    등록된 게시글이 없습니다.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>

          {/* 페이지네이션 UI */}
          <div className="flex justify-center mt-4 gap-2">
            <Button onClick={handlePreviousPage} disabled={page === 1}>
              이전
            </Button>
            {Array.from({ length: totalPages }, (_, i) => i + 1).map(
              (pageNum) => (
                <Button
                  key={pageNum}
                  onClick={() => setPage(pageNum)}
                  variant={pageNum === page ? "default" : "outline"} // 현재 페이지 강조
                >
                  {pageNum}
                </Button>
              )
            )}
            <Button onClick={handleNextPage} disabled={page === totalPages}>
              다음
            </Button>
          </div>
        </>
      )}
    </div>
  );
}
