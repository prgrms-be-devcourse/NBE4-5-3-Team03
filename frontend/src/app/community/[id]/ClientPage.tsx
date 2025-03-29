import { components } from "@/lib/backend/apiV1/schema";

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  const formatter = new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "numeric",
    day: "numeric",
    hour: "numeric",
    minute: "numeric",
    second: "numeric",
    hour12: true,
  });
  let formattedDate = formatter.format(date);
  formattedDate = formattedDate
    .replace("오전 ", "")
    .replace("오후 ", "")
    .replace("밤 ", "")
    .replace("새벽 ", "");
  return formattedDate;
};

interface Props {
  post: components["schemas"]["PostResponseDto"];
}

const ClientPage: React.FC<Props> = ({ post }) => {
  if (!post) {
    return <div>게시글을 불러오는 중...</div>;
  }

  return (
    <div className="max-w-3xl mx-auto p-8">
      <h1 className="text-2xl font-bold mb-4">{post.title}</h1>
      <div className="mb-2 text-gray-600">
        작성자: {post.nickname || "알 수 없음"}
      </div>
      <div className="mb-4 text-gray-600">
        작성일: {post.createdAt ? formatDate(post.createdAt) : "알 수 없음"}
      </div>
      <div className="whitespace-pre-line">{post.content}</div>
      {post.isSpoiler && (
        <div className="mt-4 text-sm text-red-500">
          이 게시글은 스포일러를 포함하고 있습니다.
        </div>
      )}
    </div>
  );
};

export default ClientPage;
