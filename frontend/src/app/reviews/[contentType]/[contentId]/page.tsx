import client from "@/lib/backend/client";
import ClientPage from "@/app/reviews/[contentType]/[contentId]/ClientPage";

export default async function Page({
  params,
}: {
  params: {
    contentType: string;
    contentId: string;
  };
}) {
  const { contentType, contentId } = params; // 로그
  console.log(`Fetching reviews for ${contentType} with id: ${contentId}`); // 특정 영화의 리뷰 목록 가져오기

  const response = await client.GET(
    `/api/reviews/${contentType}/${contentId}` as any,
    {
      params: { path: { contentType: contentType, contentId: contentId } },
    }
  );

  if (response.error) {
    console.error("리뷰 가져오기 실패:", response.error);
    return <div>에러 발생: {response.error.message}</div>;
  }

  return <ClientPage contentType={contentType} contentId={contentId} />;
}
