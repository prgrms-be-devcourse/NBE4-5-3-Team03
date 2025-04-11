import { cookies } from "next/headers";
import ClientPage from "./ClientPage";
import { fetchUserProfileServer } from "@/lib/api/user";

export default async function Page() {
  const cookieHeader = cookies().toString();
  const user = await fetchUserProfileServer(cookieHeader);

  if (!user || user.role !== "ADMIN") {
    return <div>권한이 없습니다.</div>;
  }

  return <ClientPage />;
}
