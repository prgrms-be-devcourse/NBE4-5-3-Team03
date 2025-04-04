import client from "@/lib/backend/client";
import ClientPage from "./ClientPage";
import { components } from "@/lib/backend/apiV1/schema";

export default async function Page({
  params,
}: {
  params: {
    id: number;
  };
}) {
  const { id } = await params;

  try {
    const response = await client.GET("/api/posts/{id}", {
      params: {
        path: {
          id: id,
        },
      },
    });

    // API 응답 데이터 검증
    if (!response || !response.data || !response.data.data) {
      return <div>게시글 정보를 불러오는 데 실패했습니다.</div>;
    }

    const postData = response.data
      .data as components["schemas"]["PostResponseDto"];

    console.log("postData in [id]/page.tsx:", postData);

    return <ClientPage post={postData} />;
  } catch (error) {
    console.error("게시글 상세 정보 불러오기 오류:", error);

    return <div>게시글 상세 정보를 불러오는 중 오류가 발생했습니다.</div>;
  }
}
