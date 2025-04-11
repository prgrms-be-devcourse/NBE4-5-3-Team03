import client from "@/lib/backend/client";
import ClientPage from "./ClientPage";
import { cookies } from "next/headers";
import { fetchUserProfileServer } from "@/lib/api/user";

export default async function Page({
  params,
}: {
  params: {
    id: number;
  };
}) {
  const cookieHeader = cookies().toString();
  const user = await fetchUserProfileServer(cookieHeader);

  if (!user || user.role !== "ADMIN") {
    return <div>권한이 없습니다.</div>;
  }

  const { id } = await params;

  const response = await client.GET("/api/movies/{id}", {
    params: {
      path: {
        id,
      },
    },
    credentials: "include",
  });

  if (response.error) {
    return <div>{response.error.message}</div>;
  }

  const data = response.data.data!!;

  return <ClientPage data={data} />;
}
