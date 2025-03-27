import client from "@/lib/backend/client";
import { components } from "@/lib/backend/apiV1/schema";
import ClientPage from "./ClientPage";

export default async function Page({
  searchParams,
}: {
  searchParams: {
    keyword: string;
    keywordType: string;
    page: number;
    pageSize: number;
  };
}) {
  const {
    keyword = "",
    keywordType = "",
    page = 1,
    pageSize = 10,
  } = await searchParams;

  try {
    const response = await client.GET("/api/posts", {
      params: {
        query: {
          keyword,
          keywordType,
          page,
          pageSize,
        },
      },
    });

    if (response.error) {
      return <div>{response.error.message}</div>;
    }

    const responseData =
      response.data as components["schemas"]["ResponseDtoPageDtoPostResponseDto"];

    const data = responseData.data!!;

    return (
      <ClientPage
        data={data}
        keyword={keyword}
        keywordType={keywordType}
        pageSize={pageSize}
        page={page}
      />
    );
  } catch (error: any) {
    console.error("게시글 목록을 불러오는 중 오류가 발생했습니다:", error);
    return <div>게시글 목록을 불러오는 중 오류가 발생했습니다.</div>;
  }
}
