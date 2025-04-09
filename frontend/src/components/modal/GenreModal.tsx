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

type GenreDto = components["schemas"]["GenreDto"];

interface Props {
  open: boolean;
  onClose: () => void;
  onSelect: (genre: GenreDto) => void;
}

export default function GenreModal({ open, onClose, onSelect }: Props) {
  const [keyword, setKeyword] = useState("");
  const [results, setResults] = useState<GenreDto[]>([]);

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
      const res = await client.GET("/api/genres", {
        params: { query: { keyword } },
      });
      if (!res.error) {
        setResults(res.data.data ?? []);
      }
    }, 300);
    return () => clearTimeout(timeout);
  }, [keyword]);

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>장르 검색</DialogTitle>
        </DialogHeader>
        <Input
          placeholder="장르 이름을 입력하세요"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <div className="max-h-60 overflow-auto mt-4 space-y-2">
          {results.map((genre) => (
            <div
              key={genre.id}
              className="flex items-center justify-between p-2 border rounded-md"
            >
              <span>{genre.name}</span>
              <Button onClick={() => onSelect(genre)} size="sm">
                선택
              </Button>
            </div>
          ))}
        </div>
      </DialogContent>
    </Dialog>
  );
}
