import client from "@/lib/backend/client";
import ClientPage from "./ClientPage";
import { cookies } from "next/headers";
import { fetchUserProfileServer } from "@/lib/api/user";

export default async function Page({ params }: { params: { id: number } }) {
  const { id } = await params;

  try {
    const response = await client.GET("/api/directors/{id}", {
      params: {
        path: { id },
      },
    });

    // API 응답 데이터 검증
    if (!response || !response.data) {
      return <div>감독 정보를 불러오는 데 실패했습니다.</div>;
    }

    const cookieHeader = cookies().toString();
    const user = await fetchUserProfileServer(cookieHeader);

    const isAdmin = user !== null && user.role === "ADMIN";

    return <ClientPage director={response.data.data} isAdmin={isAdmin} />;
  } catch (error) {
    console.error("에러 발생:", error);
    return <div>에러 발생: {String(error)}</div>;
  }
}
