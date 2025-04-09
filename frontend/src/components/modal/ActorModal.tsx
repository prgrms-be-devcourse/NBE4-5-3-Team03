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

type Actor = components["schemas"]["ActorDto"];

interface ActorSearchModalProps {
  open: boolean;
  onClose: () => void;
  onSelect: (actor: Actor) => void;
}

export default function ActorSearchModal({
  open,
  onClose,
  onSelect,
}: ActorSearchModalProps) {
  const [keyword, setKeyword] = useState("");
  const [results, setResults] = useState<Actor[]>([]);

  useEffect(() => {
    if (keyword.trim() === "") return;
    const timeout = setTimeout(async () => {
      const res = await client.GET("/api/actors", {
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
          <DialogTitle>배우 검색</DialogTitle>
        </DialogHeader>
        <Input
          placeholder="배우 이름을 입력하세요"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <div className="max-h-60 overflow-auto mt-4 space-y-2">
          {results.map((actor) => (
            <div
              key={actor.id}
              className="flex items-center justify-between p-2 border rounded-md"
            >
              <span>{actor.name}</span>
              <Button onClick={() => onSelect(actor)} size="sm">
                선택
              </Button>
            </div>
          ))}
        </div>
      </DialogContent>
    </Dialog>
  );
}
