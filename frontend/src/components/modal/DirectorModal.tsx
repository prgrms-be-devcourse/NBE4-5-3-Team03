"use client";

import React, { useState, useEffect } from "react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import client from "@/lib/backend/client";
import { components } from "@/lib/backend/apiV1/schema";
import { useRouter } from "next/navigation";

type Director = components["schemas"]["DirectorDto"];

interface Props {
  open: boolean;
  onClose: () => void;
  onSelect: (director: Director) => void;
}

export default function DirectorSearchModal({
  open,
  onClose,
  onSelect,
}: Props) {
  const [keyword, setKeyword] = useState("");
  const [results, setResults] = useState<Director[]>([]);

  const router = useRouter();

  // 모달 열릴 때 keyword, results 초기화
  useEffect(() => {
    if (open) {
      setKeyword("");
      setResults([]);
    }
  }, [open]);

  useEffect(() => {
    if (keyword.trim() === "") return;
    const timeout = setTimeout(async () => {
      const res = await client.GET("/api/directors", {
        params: { query: { keyword, page: 1, pageSize: 10 } },
      });
      if (!res.error) {
        setResults(res.data.data?.items ?? []);
      }
    }, 300);
    return () => clearTimeout(timeout);
  }, [keyword]);

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>감독 검색</DialogTitle>
        </DialogHeader>
        <Input
          placeholder="감독 이름을 입력하세요"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <div className="max-h-60 overflow-auto mt-4 space-y-2">
          {results.map((director) => (
            <div
              key={director.id}
              className="flex items-center justify-between p-2 border rounded-md"
            >
              <span>{director.name}</span>
              <Button onClick={() => onSelect(director)} size="sm">
                선택
              </Button>
            </div>
          ))}
        </div>

        {/* 감독 등록하기 버튼 */}
        <div className="pt-4 text-right">
          <Button
            variant="outline"
            onClick={() => {
              onClose(); // 모달 닫기
              router.push("/directors/create"); // 등록 페이지로 이동
            }}
          >
            + 감독 등록하기
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
