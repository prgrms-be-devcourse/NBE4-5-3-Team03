"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import client from "@/lib/backend/client";
import { components } from "@/lib/backend/apiV1/schema";

export default function DirectorUpdatePage({
  data,
}: {
  data: components["schemas"]["DirectorResponse"];
}) {
  const router = useRouter();
  const [name, setName] = useState(data.name);
  const [profilePath, setProfilePath] = useState(data.profilePath);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!name.trim()) {
      alert("감독 이름은 필수입니다.");
      return;
    }

    const res = await client.PUT("/api/directors/{id}", {
      params: {
        path: {
          id: data.id,
        },
      },
      body: {
        name,
        profilePath,
      },
      credentials: "include",
    });

    if (res.error) {
      alert(res.error.message);
    } else {
      alert("수정되었습니다.");
      router.push(`/directors/${res.data.data!!.id}`);
    }
  };

  return (
    <div className="max-w-xl mx-auto mt-10">
      <Card className="p-6 shadow-xl rounded-2xl">
        <CardHeader>
          <CardTitle className="text-3xl font-bold">감독 수정</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <Label>이름</Label>
              <Input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="이름을 입력하세요"
              />
            </div>
            <div>
              <Label>프로필 이미지</Label>
              <Input
                value={profilePath}
                onChange={(e) => setProfilePath(e.target.value)}
                placeholder="프로필 이미지 경로를 입력하세요"
              />
            </div>
            <div className="text-right pt-6 flex justify-end gap-2">
              <Button
                type="submit"
                className="px-6 py-2 text-base font-semibold"
              >
                수정
              </Button>
              <Button
                type="button"
                variant="destructive"
                onClick={() => router.back()}
                className="px-6 py-2 text-base font-semibold"
              >
                취소
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
