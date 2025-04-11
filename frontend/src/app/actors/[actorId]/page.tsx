import client from "@/lib/backend/client";
import ClientPage from "./ClientPage";
import { cookies } from "next/headers";
import { fetchUserProfileServer } from "@/lib/api/user";

export default async function Page({
  params,
}: {
  params: { actorId: number };
}) {
  const { actorId } = await params;

  try {
    const response = await client.GET("/api/actors/{actorId}", {
      params: {
        path: { actorId },
      },
    });

    // API 응답 데이터 검증
    if (!response || !response.data || !response.data.data) {
      return <div>배우 정보를 불러오는 데 실패했습니다.</div>;
    }

    const cookieHeader = cookies().toString();
    const user = await fetchUserProfileServer(cookieHeader);

    const isAdmin = user !== null && user.role === "ADMIN";

    return <ClientPage actor={response.data.data} isAdmin={isAdmin} />;
  } catch (error) {
    console.error("에러 발생:", error);
    return <div>에러 발생: {String(error)}</div>;
  }
}
