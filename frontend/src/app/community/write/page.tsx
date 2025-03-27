"use client";

import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { components } from "@/lib/backend/apiV1/schema";
import client from "@/lib/backend/client";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { fetchUserProfileClient } from "@/lib/api/user";

export default function Page() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [isSpoiler, setIsSpoiler] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [userAccountId, setUserAccountId] = useState<number | null>(null);

  useEffect(() => {
    const fetchUser = async () => {
      const userData = await fetchUserProfileClient();
      if (userData && userData.id) {
        setUserAccountId(userData.id);
      } else {
        setUserAccountId(null);
        alert("로그인 후 게시글을 작성할 수 있습니다.");
        // 로그인 페이지로 리다이렉트
        router.push("/login");
      }
    };

    fetchUser();
  }, [router]);

  const handleTitleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setTitle(event.target.value);
  };

  const handleContentChange = (
    event: React.ChangeEvent<HTMLTextAreaElement>
  ) => {
    setContent(event.target.value);
  };

  const handleSpoilerChange = (checked: boolean) => {
    setIsSpoiler(checked);
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setErrorMessage(null);

    if (!userAccountId) {
      alert("로그인 후 게시글을 작성할 수 있습니다.");
      return;
    }

    try {
      const response = await client.POST("/api/posts", {
        body: {
          userAccountId: userAccountId,
          title,
          content,
          isSpoiler,
        } as components["schemas"]["PostCreateRequestDto"],
      });

      if (response.error) {
        setErrorMessage(
          response.error.message ?? "알 수 없는 오류가 발생했습니다."
        );
        return;
      }

      // 게시글 생성 성공 후 리다이렉트
      router.push("/community");
    } catch (error: any) {
      console.error("게시글 등록을 실패했습니다.: ", error);
      setErrorMessage("게시글 등록 중 오류가 발생했습니다.");
    }
  };

  const handleCancel = () => {
    router.push("/community");
  };

  return (
    <div className="max-w-3xl mx-auto p-8">
      <h1 className="text-2xl font-bold mb-6">새 게시글 작성</h1>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label
            htmlFor="title"
            className="block text-gray-700 text-sm font-bold mb-2"
          >
            제목
          </label>
          <Input
            type="text"
            id="title"
            value={title}
            onChange={handleTitleChange}
            placeholder="제목을 입력하세요"
            required
          />
        </div>
        <div>
          <label
            htmlFor="content"
            className="block text-gray-700 text-sm font-bold mb-2"
          >
            내용
          </label>
          <Textarea
            id="content"
            value={content}
            onChange={handleContentChange}
            placeholder="내용을 입력하세요"
            rows={8}
            required
          />
        </div>
        <div>
          <Checkbox
            id="spoiler"
            checked={isSpoiler}
            onCheckedChange={handleSpoilerChange}
          />
          <label htmlFor="spoiler" className="ml-2 text-gray-700 text-sm">
            스포일러 포함
          </label>
        </div>
        <div className="flex gap-2">
          <Button type="submit" variant="default">
            등록
          </Button>
          <Button type="button" variant="secondary" onClick={handleCancel}>
            취소
          </Button>
        </div>
        {errorMessage && <p className="text-red-500">{errorMessage}</p>}
      </form>
    </div>
  );
}
