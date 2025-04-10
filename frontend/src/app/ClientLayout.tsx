"use client";

import Link from "next/link";
import { useAuth } from "@/lib/hooks/useAuth";
import { useEffect, useState } from "react";

export default function ClientLayout({
  children,
  fontVariable,
  fontClassName,
}: Readonly<{
  children: React.ReactNode;
  fontVariable: string;
  fontClassName: string;
}>) {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  // 배우나 감독을 눌러서 이동하면 드롭다운이 닫힌다.
  const handleLinkClick = () => {
    setIsDropdownOpen(false);
  };

  const { isAuthenticated, logout, user } = useAuth();
  const [authState, setAuthState] = useState(isAuthenticated);

  const [isRegisterDropdownOpen, setIsRegisterDropdownOpen] = useState(false);

  const handleDropdownClose = () => {
    setIsDropdownOpen(false);
    setIsRegisterDropdownOpen(false);
  };

  // 인증 상태 변경 감지하여 UI 강제 업데이트
  useEffect(() => {
    setAuthState(isAuthenticated);
  }, [isAuthenticated]);

  return (
    <html lang="ko" className={`${fontVariable}`}>
      <body className={`min-h-[100dvh] flex flex-col ${fontClassName}`}>
        {/* 헤더 */}
        <header className="flex justify-between items-center px-6 py-4 bg-gray-800 text-white">
          <div className="flex items-center gap-20">
            <h1 className="text-2xl font-bold">
              <Link href="/">🎬 Flicktionary</Link>
            </h1>
            <nav className="flex gap-8">
              <Link href="/movies" className="hover:text-gray-300">
                영화
              </Link>
              <Link href="/series" className="hover:text-gray-300">
                시리즈
              </Link>
              {/* Dropdown for 인물 */}
              <div className="relative">
                <button
                  onClick={() => {
                    setIsDropdownOpen(!isDropdownOpen);
                    setIsRegisterDropdownOpen(false);
                  }}
                  className="hover:text-gray-300"
                >
                  인물
                </button>
                {isDropdownOpen && (
                  <div className="absolute bg-gray-800 text-white w-32 mt-2 rounded shadow-lg">
                    <Link
                      href="/actors"
                      className="block px-4 py-2 hover:bg-gray-700"
                      onClick={handleLinkClick}
                    >
                      배우
                    </Link>
                    <Link
                      href="/directors"
                      className="block px-4 py-2 hover:bg-gray-700"
                      onClick={handleLinkClick}
                    >
                      감독
                    </Link>
                  </div>
                )}
              </div>
              <Link href="/community" className="hover:text-gray-300">
                커뮤니티
              </Link>

              {/* 등록 드롭다운 메뉴 */}
              <div className="relative">
                <button
                  onClick={() => {
                    setIsRegisterDropdownOpen(!isRegisterDropdownOpen);
                    setIsDropdownOpen(false);
                  }}
                  className="hover:text-gray-300"
                >
                  등록
                </button>
                {isRegisterDropdownOpen && (
                  <div className="absolute bg-gray-800 text-white w-40 mt-2 rounded shadow-lg z-10">
                    <Link
                      href="/movies/create"
                      className="block px-4 py-2 hover:bg-gray-700"
                      onClick={handleDropdownClose}
                    >
                      영화 등록
                    </Link>
                    <Link
                      href="/series/create"
                      className="block px-4 py-2 hover:bg-gray-700"
                      onClick={handleDropdownClose}
                    >
                      시리즈 등록
                    </Link>
                    <Link
                      href="/actors/create"
                      className="block px-4 py-2 hover:bg-gray-700"
                      onClick={handleDropdownClose}
                    >
                      배우 등록
                    </Link>
                    <Link
                      href="/directors/create"
                      className="block px-4 py-2 hover:bg-gray-700"
                      onClick={handleDropdownClose}
                    >
                      감독 등록
                    </Link>
                    <Link
                      href="/genres/create"
                      className="block px-4 py-2 hover:bg-gray-700"
                      onClick={handleDropdownClose}
                    >
                      장르 등록
                    </Link>
                  </div>
                )}
              </div>
            </nav>
          </div>

          {/* 로그인 상태에 따라 버튼 표시 */}
          <nav className="flex gap-4">
            {authState ? (
              <>
                {/* 닉네임 및 역할 표시 */}
                {user?.role === "USER" && (
                  <span>{user.username}님</span> // 일반 유저는 닉네임만
                )}
                {user?.role === "ADMIN" && (
                  <span>{user.username}님 / ADMIN</span> // 관리자는 닉네임 + ADMIN
                )}

                {user?.role === "USER" && (
                  <Link href="/my/favorites" className="hover:text-gray-300">
                    즐겨찾기
                  </Link>
                )}
                {user?.role === "ADMIN" && (
                  <Link href="/admin/reviews" className="hover:text-gray-300">
                    리뷰 관리
                  </Link>
                )}
                {user?.role === "ADMIN" && (
                  <Link href="/admin/community" className="hover:text-gray-300">
                    커뮤니티 관리
                  </Link>
                )}
                <Link href="/my/profile" className="hover:text-gray-300">
                  내 정보
                </Link>
                <button onClick={logout} className="hover:text-gray-300">
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link href="/login" className="hover:text-gray-300">
                  로그인
                </Link>
                <Link href="/signup" className="hover:text-gray-300">
                  회원 가입
                </Link>
              </>
            )}
          </nav>
        </header>

        {/* 메인 콘텐츠 */}
        <div className="flex-grow">{children}</div>

        {/* 푸터 */}
        <footer className="py-4 text-center bg-gray-800 text-white">
          © {new Date().getFullYear()} Flicktionary. All rights reserved.
        </footer>
      </body>
    </html>
  );
}
