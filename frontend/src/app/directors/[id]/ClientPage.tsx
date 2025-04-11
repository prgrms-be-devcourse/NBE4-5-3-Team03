"use client";

import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import client from "@/lib/backend/client";

interface Movie {
  id: number;
  title: string;
  posterPath: string;
  releaseDate: string;
}

interface Series {
  id: number;
  title: string;
  posterPath: string;
  releaseStartDate: string;
  releaseEndDate: string;
}

interface Director {
  id: number;
  name: string;
  profilePath: string;
  movies: Movie[];
  series: Series[];
}

export default function DirectorDetailPage({
  director,
  isAdmin,
}: {
  director: Director;
  isAdmin: boolean;
}) {
  const router = useRouter();

  const handleDelete = async () => {
    if (confirm("정말 이 배우를 삭제하시겠습니까?")) {
      const res = await client.DELETE("/api/directors/{id}", {
        params: { path: { id: director.id } },
      });

      if (res.error) {
        alert(res.error.message);
      } else {
        alert("삭제되었습니다.");
        router.push("/directors");
      }
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <h1 className="text-3xl font-bold mb-6 text-center">🎭 감독 정보</h1>

      {/* 감독 정보 */}
      <div className="flex flex-col md:flex-row items-center space-x-6 bg-white shadow-md p-6 rounded-lg">
        <img
          src={director.profilePath || "/no-image.png"}
          alt={director.name}
          width={150}
          height={150}
          className="rounded-lg"
        />
        <div className="flex-1 mt-4 md:mt-0">
          <h1 className="text-2xl font-bold">{director.name}</h1>
          {isAdmin && (
            <div className="flex justify-end space-x-2 mt-4">
              <Button
                variant="default"
                onClick={() => router.push(`/directors/edit/${director.id}`)}
              >
                수정
              </Button>
              <Button variant="destructive" onClick={handleDelete}>
                삭제
              </Button>
            </div>
          )}
        </div>
      </div>

      {/* 연출 영화 리스트 */}
      <div className="mt-8">
        <h2 className="text-xl font-semibold mb-4">🎬 연출한 영화</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {director.movies.length > 0 ? (
            director.movies.map((movie) => (
              <div
                key={movie.id}
                className="bg-white shadow-md p-4 rounded-lg cursor-pointer hover:bg-gray-50 transition"
                onClick={() => router.push(`/movies/${movie.id}`)}
              >
                <img
                  src={movie.posterPath || "/default-movie.png"}
                  alt={movie.title}
                  width={200}
                  height={300}
                  className="rounded-md"
                />
                <h3 className="text-lg font-medium mt-2">{movie.title}</h3>
                <p className="text-gray-500">{movie.releaseDate}</p>
              </div>
            ))
          ) : (
            <p className="text-gray-500">연출한 영화가 없습니다.</p>
          )}
        </div>
      </div>

      {/* 출연한 시리즈 리스트 */}
      <div className="mt-8">
        <h2 className="text-xl font-semibold mb-4">📺 연출한 시리즈</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {director.series.length > 0 ? (
            director.series.map((series) => (
              <div
                key={series.id}
                className="bg-white shadow-md p-4 rounded-lg cursor-pointer hover:bg-gray-50 transition"
                onClick={() => router.push(`/series/${series.id}`)}
              >
                <img
                  src={series.posterPath || "/default-series.png"}
                  alt={series.title}
                  width={200}
                  height={300}
                  className="rounded-md"
                />
                <h3 className="text-lg font-medium mt-2">{series.title}</h3>
                <p className="text-gray-500">
                  {series.releaseStartDate} ~{" "}
                  {series.releaseEndDate || "방영 중"}
                </p>
              </div>
            ))
          ) : (
            <p className="text-gray-500">연출한 시리즈가 없습니다.</p>
          )}
        </div>
      </div>

      {/* 돌아가기 버튼 */}
      <div className="flex justify-center mt-8">
        <Button variant="outline" onClick={() => router.back()}>
          🔙 돌아가기
        </Button>
      </div>
    </div>
  );
}
