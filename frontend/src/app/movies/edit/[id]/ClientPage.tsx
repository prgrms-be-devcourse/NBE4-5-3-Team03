"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import client from "@/lib/backend/client";
import { components } from "@/lib/backend/apiV1/schema";
import GenreModal from "@/components/modal/GenreModal";
import ActorSearchModal from "@/components/modal/ActorModal";
import DirectorModal from "@/components/modal/DirectorModal";

export default function MovieUpdatePage({
  data,
}: {
  data: components["schemas"]["MovieResponseWithDetail"];
}) {
  const router = useRouter();

  const [title, setTitle] = useState(data.title);
  const [overview, setOverview] = useState(data.overview);
  const [releaseDate, setReleaseDate] = useState(data.releaseDate);
  const [status, setStatus] = useState(data.status);
  const [posterPath, setPosterPath] = useState(data.posterPath);
  const [runtime, setRuntime] = useState(data.runtime);
  const [productionCountry, setProductionCountry] = useState(
    data.productionCompany
  );
  const [productionCompany, setProductionCompany] = useState(
    data.productionCountry
  );
  const [genres, setGenres] = useState<components["schemas"]["GenreDto"][]>(
    data.genres
  );
  const [casts, setCasts] = useState<
    {
      id: number;
      name: string;
      characterName: string;
    }[]
  >(
    data.casts.map((c) => ({
      id: c.actor.id,
      name: c.actor.name,
      characterName: c.characterName,
    }))
  );
  const [director, setDirector] = useState<
    components["schemas"]["DirectorDto"]
  >(data.director);

  const [genreModalOpen, setGenreModalOpen] = useState(false);
  const [actorModalOpen, setActorModalOpen] = useState(false);
  const [directorModalOpen, setDirectorModalOpen] = useState(false);

  const handleSelectedActor = (actor: components["schemas"]["ActorDto"]) => {
    if (!casts.find((c) => c.id === actor.id)) {
      setCasts((prev) => [
        ...prev,
        { id: actor.id, name: actor.name, characterName: "" },
      ]);
    }
    setActorModalOpen(false);
  };

  const updateCast = (index: number, key: string, value: string) => {
    setCasts((prev) =>
      prev.map((cast, i) => (i === index ? { ...cast, [key]: value } : cast))
    );
  };

  const handleSelectDirector = (
    director: components["schemas"]["DirectorDto"]
  ) => {
    setDirector({ id: director.id, name: director.name });
    setDirectorModalOpen(false);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (director == null || director == undefined) {
      alert("감독을 선택해주세요");
      return;
    }

    const res = await client.PUT("/api/movies/{id}", {
      params: {
        path: {
          id: data.id,
        },
      },
      body: {
        title,
        overview,
        releaseDate,
        status,
        posterPath,
        runtime,
        productionCountry,
        productionCompany,
        genreIds: genres.map((g) => g.id),
        casts: casts.map((c) => ({
          actorId: c.id,
          characterName: c.characterName,
        })),
        directorId: director!!.id,
      },
    });

    if (res.error) {
      alert(res.error.message);
    } else {
      router.push(`/movies/${res.data.data!!.id}`);
    }
  };

  return (
    <div className="max-w-4xl mx-auto mt-10">
      <Card className="p-6 shadow-xl rounded-2xl">
        <CardHeader>
          <CardTitle className="text-3xl font-bold">영화 수정</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label>제목</Label>
                <Input
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                />
              </div>
              <div>
                <Label>상영 시간</Label>
                <Input
                  type="number"
                  value={runtime}
                  onChange={(e) => setRuntime(+e.target.value)}
                />
              </div>
              <div>
                <Label>개봉일</Label>
                <Input
                  type="date"
                  value={releaseDate}
                  onChange={(e) => setReleaseDate(e.target.value)}
                />
              </div>
              <div>
                <Label>상태</Label>
                <Input
                  value={status}
                  onChange={(e) => setStatus(e.target.value)}
                />
              </div>
              <div className="col-span-2">
                <Label>포스터 경로</Label>
                <Input
                  value={posterPath}
                  onChange={(e) => setPosterPath(e.target.value)}
                />
              </div>
            </div>

            <div>
              <Label>줄거리</Label>
              <textarea
                value={overview}
                onChange={(e) => setOverview(e.target.value)}
                rows={4}
                className="w-full border rounded p-2"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label>제작 국가</Label>
                <Input
                  value={productionCountry}
                  onChange={(e) => setProductionCountry(e.target.value)}
                />
              </div>
              <div>
                <Label>제작사</Label>
                <Input
                  value={productionCompany}
                  onChange={(e) => setProductionCompany(e.target.value)}
                />
              </div>
            </div>

            <div>
              <Label>장르</Label>
              <div className="flex flex-wrap gap-2 mt-2">
                {genres.map((genre) => (
                  <div
                    key={genre.id}
                    className="flex items-center bg-gray-100 px-3 py-1 rounded-full"
                  >
                    {genre.name}
                    <button
                      type="button"
                      className="ml-2 text-red-500"
                      onClick={() =>
                        setGenres(genres.filter((g) => g.id !== genre.id))
                      }
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
              <Button
                type="button"
                className="mt-2"
                onClick={() => setGenreModalOpen(true)}
              >
                장르 추가
              </Button>
              <GenreModal
                open={genreModalOpen}
                onClose={() => setGenreModalOpen(false)}
                onSelect={(genre) => {
                  if (!genres.find((g) => g.id === genre.id)) {
                    setGenres([...genres, genre]);
                  }
                }}
              />
            </div>

            <div>
              <Label>출연진</Label>
              <div className="space-y-2 mt-2">
                {casts.map((cast, index) => (
                  <div key={index} className="flex gap-2 items-center">
                    <span className="font-medium min-w-[10rem]">
                      {cast.name}
                    </span>{" "}
                    <Input
                      placeholder="역할 이름"
                      value={cast.characterName}
                      onChange={(e) =>
                        updateCast(index, "characterName", e.target.value)
                      }
                    />
                    <button
                      type="button"
                      className="text-red-500 ml-2"
                      onClick={() =>
                        setCasts(casts.filter((_, i) => i !== index))
                      }
                    >
                      ×
                    </button>
                  </div>
                ))}
                <Button type="button" onClick={() => setActorModalOpen(true)}>
                  출연진 추가
                </Button>
              </div>
              <ActorSearchModal
                open={actorModalOpen}
                onClose={() => setActorModalOpen(false)}
                onSelect={handleSelectedActor}
              />
            </div>

            <div>
              <Label>감독</Label>
              <div className="flex items-center gap-2 mt-2">
                <div className="flex-1">
                  {director ? (
                    <div className="flex items-center justify-between border rounded px-2 py-1">
                      {director.name}
                      <button
                        type="button"
                        onClick={() => setDirector(undefined)}
                        className="text-red-500 ml-2"
                      >
                        ×
                      </button>
                    </div>
                  ) : (
                    <div className="text-gray-500">감독을 선택해주세요</div>
                  )}
                </div>
                <Button
                  type="button"
                  onClick={() => setDirectorModalOpen(true)}
                >
                  감독 선택
                </Button>
                <DirectorModal
                  open={directorModalOpen}
                  onClose={() => setDirectorModalOpen(false)}
                  onSelect={handleSelectDirector}
                />
              </div>
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
