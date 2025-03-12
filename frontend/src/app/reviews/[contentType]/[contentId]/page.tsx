import client from "@/lib/backend/client";
import ClientPage from "@/app/reviews/[contentType]/[contentId]/ClientPage";

interface PageProps {
  params: {
    contentType: string;
    contentId: string;
  };
}

export default async function Page({ params }: PageProps) {
  const { contentType, contentId } = params;

  // 로그
  console.log(`Fetching reviews for ${contentType} with id: ${contentId}`);

  // "/api/reviews/" 로 시작
  let apiUrl = `/api/reviews/`;

  if (contentType === "movies") {
    // 영화 리뷰 목록 조회 경로: "/api/reviews/movie/{movie_id}"
    apiUrl += `movies/${contentId}`;
  } else if (contentType === "series") {
    // 드라마 리뷰 목록 조회 경로: "/api/reviews/series/{series_id}"
    apiUrl += `series/${contentId}`;
  } else {
    // 유효하지 않은 contentType 처리
    console.error("잘못된 contentType:", contentType);
    // 또는 사용자에게 에러 메시지 표시
    return <div>잘못된 컨텐츠 타입입니다.</div>;
  }

  // API 요청 URL 로그
  console.log("API 요청 URL:", apiUrl);

  const response = await client.GET(apiUrl as any, {});

  console.log("page.tsx - 서버 컴포넌트 API 응답:", response); // 👈 **[추가]** 서버 컴포넌트 API 응답 데이터 로그 출력

  if (response.error) {
    console.error("리뷰 가져오기 실패:", response.error);
    return <div>에러 발생: {response.error.message}</div>;
  }

  return <ClientPage contentType={contentType} contentId={contentId} />;
}
