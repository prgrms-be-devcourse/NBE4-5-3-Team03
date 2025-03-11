import client from "@/lib/backend/client";
import ClientPage from "./ClientPage";

export default async function Page({ params }: { params: { id: string } }) {
  const { id } = params;
  const movie_id = parseInt(id, 10);
  // 기본 페이지는 0
  const page = searchParams.page ? parseInt(searchParams.page, 10) : 0;

  console.log(`Fetching reviews for movie_id: ${movie_id}, page: ${page}`);

  // 특정 영화의 리뷰 목록 가져오기
  const response = await client.GET("/api/reviews/movie/{movie_id}" as any, {
    params: { path: { movie_id }, query: { page, size: 5 } },
  });

  if (response.error) {
    console.error("리뷰 가져오기 실패:", response.error);
    return <div>에러 발생: {response.error.message}</div>;
  }

  return <ClientPage data={response.data} movieId={movie_id} />;
}
