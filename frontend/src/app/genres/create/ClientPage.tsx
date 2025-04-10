"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import client from "@/lib/backend/client";

export default function GenreCreatePage() {
  const router = useRouter();
  const [name, setName] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!name.trim()) {
      alert("장르 이름은 필수입니다.");
      return;
    }

    const res = await client.POST("/api/genres", {
      body: {
        name,
      },
    });

    if (res.error) {
      alert(res.error.message);
    } else {
      alert("장르가 성공적으로 등록되었습니다.");
      router.push("/");
    }
  };

  return (
    <div className="max-w-xl mx-auto mt-10">
      <Card className="p-6 shadow-xl rounded-2xl">
        <CardHeader>
          <CardTitle className="text-3xl font-bold">장르 등록</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <Label>장르명</Label>
              <Input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="장르명을 입력하세요"
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
