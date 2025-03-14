import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

const API_BASE_URL = "http://localhost:8080"; // Spring Boot 서버 주소

export const useAuth = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const router = useRouter();

  // 인증 상태 확인
  const checkAuthStatus = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/users/status`, {
        method: "GET",
        credentials: "include",
      });

      if (response.ok) {
        setIsAuthenticated(true);
      } else {
        setIsAuthenticated(false);
      }
    } catch (error) {
      console.error("인증 상태 확인 실패:", error);
      setIsAuthenticated(false);
    }
  };

  // 일정 간격으로 인증 상태 확인 (5초마다)
  useEffect(() => {
    checkAuthStatus(); // 첫 실행
    const interval = setInterval(checkAuthStatus, 5000); // 5초마다 실행
    return () => clearInterval(interval); // 컴포넌트 언마운트 시 정리
  }, []);

  // 로그아웃 함수
  const logout = async () => {
    await fetch(`${API_BASE_URL}/api/users/logout`, {
      method: "GET",
      credentials: "include",
    });

    setIsAuthenticated(false); // UI 즉시 변경
    await checkAuthStatus(); // 강제 인증 상태 확인 후 이동
    router.push("/"); // 로그아웃 후 메인 페이지로 이동
  };

  // 회원가입 후 인증 초기화 (자동 로그인 방지)
  const clearAuthCookies = async () => {
    await fetch(`${API_BASE_URL}/api/users/logout`, {
      method: "GET",
      credentials: "include",
    });
    setIsAuthenticated(false);
  };

  return {
    isAuthenticated,
    logout,
    setIsAuthenticated,
    checkAuthStatus,
    clearAuthCookies,
  };
};
