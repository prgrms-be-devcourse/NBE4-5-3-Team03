"use client";

import { components } from "@/lib/backend/apiV1/schema";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { fetchUserProfileClient } from "@/lib/api/user";

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
  const [isEditing, setIsEditing] = useState(false);
  const [editedTitle, setEditedTitle] = useState(post.title);
  const [editedContent, setEditedContent] = useState(post.content);
  const [editedIsSpoiler, setEditedIsSpoiler] = useState(post.isSpoiler);
  const [currentPost, setCurrentPost] = useState<
    components["schemas"]["PostResponseDto"] | null
  >(post);

  if (!post) {
    return <div>게시글을 불러오는 중...</div>;
  }

  const router = useRouter();
  const [loggedInUserId, setLoggedInUserId] = useState<number | null>(null);

  useEffect(() => {
    const fetchUser = async () => {
      const userData = await fetchUserProfileClient();
      if (userData && userData.id) {
        setLoggedInUserId(userData.id);
      }
    };

    fetchUser();
  }, []);

  // 글목록으로 돌아가기
  const handleGoBack = () => {
    router.push("/community");
  };

  // 게시글 수정 버튼
  const handleEditClick = () => {
    setIsEditing(true);
  };

  // 게시글 수정
  const handleSaveClick = async () => {
    // 수정된 내용
    const updatedPostData = {
      title: editedTitle,
      content: editedContent,
      isSpoiler: editedIsSpoiler,
    };

    try {
      const response = await fetch(
        `http://localhost:8080/api/posts/${post.id}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(updatedPostData),
        }
      );

      if (response.ok) {
        // 수정 완료 후 보기 모드로 전환
        setIsEditing(false);

        // 수정된 게시글 정보 다시 불러오기
        const fetchUpdatedPost = async () => {
          try {
            const updatedResponse = await fetch(
              `http://localhost:8080/api/posts/${post.id}`
            );
            if (updatedResponse.ok) {
              const updatedPostDataFromServer = await updatedResponse.json();
              setCurrentPost(updatedPostDataFromServer.data);
            }
          } catch (error) {
            console.error("수정된 게시글 정보 불러오는 중 오류 발생:", error);
          }
        };

        fetchUpdatedPost();
      }
    } catch (error) {
      console.error("API 요청 중 오류 발생:", error);
    }
  };

  // 게시글 수정 취소
  const handleCancelClick = () => {
    // 수정 취소
    setIsEditing(false);
    // 이전 제목으로 복원
    setEditedTitle(post.title);
    setEditedContent(post.content);
    setEditedIsSpoiler(post.isSpoiler);
  };

  // 게시글 삭제
  const handleDeleteClick = async () => {
    if (window.confirm("정말로 삭제하시겠습니까?")) {
      try {
        const response = await fetch(
          `http://localhost:8080/api/posts/${post.id}`,
          {
            method: "DELETE",
          }
        );
        if (response.ok) {
          // 삭제 후 목록 페이지로 이동
          router.push("/community");
        }
      } catch (error) {
        console.error("API 요청 중 오류 발생:", error);
      }
    }
  };

  return (
    <div className="max-w-3xl mx-auto p-8">
      {currentPost?.isSpoiler && !isEditing && (
        <div className="mb-4 text-sm text-red-500">
          이 게시글은 스포일러를 포함하고 있습니다.
        </div>
      )}
      {isEditing ? (
        <div>
          <input
            type="text"
            className="w-full border rounded-md p-2 mb-2"
            value={editedTitle}
            onChange={(e) => setEditedTitle(e.target.value)}
          />
          <textarea
            className="w-full border rounded-md p-2 mb-4"
            value={editedContent}
            onChange={(e) => setEditedContent(e.target.value)}
            // 필요에 따라 높이 조절
            rows={10}
          />
          <div className="flex items-center mb-2">
            <input
              type="checkbox"
              id="isSpoiler"
              className="mr-2"
              checked={editedIsSpoiler}
              onChange={(e) => setEditedIsSpoiler(e.target.checked)}
            />
            <label htmlFor="isSpoiler">스포일러 여부</label>
          </div>
          <div className="flex justify-end space-x-2">
            <button
              onClick={handleSaveClick}
              className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
            >
              저장
            </button>
            <button
              onClick={handleCancelClick}
              className="bg-gray-300 hover:bg-gray-400 text-gray-800 font-bold py-2 px-4 rounded"
            >
              취소
            </button>
          </div>
        </div>
      ) : (
        <>
          <h1 className="text-2xl font-bold mb-4">{currentPost?.title}</h1>
          <div className="flex items-center mb-2 text-gray-600 space-x-4 border-b border-gray-300 pb-2">
            <span className="pr-4 border-r border-gray-300">
              작성자: {currentPost?.nickname || "탈퇴한 회원"}
            </span>
            <span>
              작성일:{" "}
              {post.createdAt
                ? formatDate(
                    currentPost?.createdAt
                      ? formatDate(currentPost.createdAt)
                      : "알 수 없음"
                  )
                : "알 수 없음"}
            </span>
          </div>
          <div className="mb-4 text-gray-600 whitespace-pre-line">
            {currentPost?.content}
          </div>
        </>
      )}
      <div className="flex justify-end mt-8 space-x-2">
        {loggedInUserId === currentPost?.userAccountId && !isEditing && (
          <>
            <button
              onClick={handleEditClick}
              className="bg-yellow-500 hover:bg-yellow-700 text-white font-bold py-2 px-4 rounded"
            >
              글 수정
            </button>
            <button
              onClick={handleDeleteClick}
              className="bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded"
            >
              삭제
            </button>
          </>
        )}
        <button
          onClick={handleGoBack}
          className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
        >
          글 목록
        </button>
      </div>
    </div>
  );
};

export default ClientPage;
