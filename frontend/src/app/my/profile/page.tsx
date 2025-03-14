"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/hooks/useAuth";

const API_BASE_URL = "http://localhost:8080"; // Spring Boot API 주소

export default function MyProfile() {
  const [user, setUser] = useState<{
    id: number;
    username: string;
    email: string;
    nickname: string;
  } | null>(null);
  const [newPassword, setNewPassword] = useState("");
  const [isEditingPassword, setIsEditingPassword] = useState(false);
  const { isAuthenticated, logout, checkAuthStatus } = useAuth();
  const router = useRouter();

  useEffect(() => {
    const fetchUserProfile = async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/api/users`, {
          method: "GET",
          credentials: "include",
        });

        if (response.ok) {
          const data = await response.json();
          setUser(data.data); // 서버 응답 형식에 맞게 수정
        } else {
          console.error("사용자 정보를 가져오지 못했습니다.");
        }
      } catch (error) {
        console.error("에러 발생:", error);
      }
    };

    fetchUserProfile();
  }, []);

  // 비밀번호 변경 요청
  const handleChangePassword = async () => {
    if (!newPassword) return alert("새 비밀번호를 입력하세요.");

    try {
      const response = await fetch(`${API_BASE_URL}/api/users/${user?.id}`, {
        method: "PUT",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ password: newPassword }),
      });

      if (response.ok) {
        alert("비밀번호가 변경되었습니다.");
        setIsEditingPassword(false);
        setNewPassword("");
      } else {
        alert("비밀번호 변경에 실패했습니다.");
      }
    } catch (error) {
      console.error("비밀번호 변경 중 에러 발생:", error);
    }
  };

  // 회원 탈퇴 요청
  const handleDeleteAccount = async () => {
    const confirmDelete = confirm(
      "정말 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.",
    );
    if (!confirmDelete) return;

    try {
      // 인증 상태 업데이트 및 로그아웃 실행
      logout();
      const response = await fetch(`${API_BASE_URL}/api/users/${user?.id}`, {
        method: "DELETE",
        credentials: "include",
      });

      if (response.ok) {
        alert("회원 탈퇴가 완료되었습니다.");
      } else {
        alert("회원 탈퇴에 실패했습니다.");
      }
    } catch (error) {
      console.error("회원 탈퇴 중 에러 발생:", error);
    }
  };

  return (
    <main className="max-w-2xl mx-auto p-6">
      <h1 className="text-2xl font-bold mb-4">내 정보</h1>
      {user ? (
        <div className="bg-gray-100 p-4 rounded-lg">
          <p>
            <strong>아이디:</strong> {user.username}
          </p>
          <p>
            <strong>이메일:</strong> {user.email}
          </p>
          <p>
            <strong>닉네임:</strong> {user.nickname || "없음"}
          </p>

          {/* 비밀번호 변경 */}
          <div className="mt-4">
            {isEditingPassword ? (
              <div className="flex gap-2">
                <input
                  type="password"
                  placeholder="새 비밀번호 입력"
                  className="border p-2 rounded"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                />
                <button
                  onClick={handleChangePassword}
                  className="bg-blue-500 text-white px-4 py-2 rounded"
                >
                  변경
                </button>
                <button
                  onClick={() => setIsEditingPassword(false)}
                  className="bg-gray-500 text-white px-4 py-2 rounded"
                >
                  취소
                </button>
              </div>
            ) : (
              <button
                onClick={() => setIsEditingPassword(true)}
                className="bg-green-500 text-white px-4 py-2 rounded"
              >
                비밀번호 변경
              </button>
            )}
          </div>

          {/* 회원 탈퇴 */}
          <div className="mt-4">
            <button
              onClick={handleDeleteAccount}
              className="bg-red-500 text-white px-4 py-2 rounded"
            >
              회원 탈퇴
            </button>
          </div>
        </div>
      ) : (
        <p>사용자 정보를 불러오는 중...</p>
      )}
    </main>
  );
}
