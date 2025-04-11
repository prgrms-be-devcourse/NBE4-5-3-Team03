import ClientPage from "./ClientPage";

export default async function Page() {
  const cookieHeader = cookies().toString();
  const user = await fetchUserProfileServer(cookieHeader);

  if (!user || user.role !== "ADMIN") {
    return <div>권한이 없습니다.</div>;
  }
  return <ClientPage />;
}
