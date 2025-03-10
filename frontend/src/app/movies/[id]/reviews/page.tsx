import client from "@/lib/backend/client";
import ClientPage from "./ClientPage";

export default async function Page({
  params,
}: {
  params: { movie_id: number };
}) {
  const { movie_id } = params;

  // 특정 영화의 리뷰 목록 가져오기
  const response = await client.GET("/api/reviews/movie/{movie_id}", {
    params: { path: { movie_id } },
  });

  if (response.error) {
    return <div>에러 발생: {response.error.message}</div>;
  }

  return <ClientPage data={response.data} movieId={movie_id} />;
}
