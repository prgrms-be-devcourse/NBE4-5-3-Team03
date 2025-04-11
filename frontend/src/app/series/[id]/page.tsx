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
  const { id } = await params;

  const response = await client.GET("/api/series/{id}", {
    params: {
      path: {
        id,
      },
    },
  });

  if (response.error) {
    return <div>{response.error.message}</div>;
  }

  const data = response.data.data!!;

  const cookieHeader = cookies().toString();
  const user = await fetchUserProfileServer(cookieHeader);

  const isAdmin = user !== null && user.role === "ADMIN";

  return <ClientPage data={data} isAdmin={isAdmin} />;
}
