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
        if (isAuthenticated) {
          // 기존에 로그인된 경우에만 인증 만료 처리
          setIsAuthenticated(false);
          alert("인증이 만료되었습니다. 다시 로그인해주세요.");
          router.push("/");
        } else {
          setIsAuthenticated(false);
        }
      }
    } catch (error) {
      console.error("인증 상태 확인 실패:", error);
      if (isAuthenticated) {
        setIsAuthenticated(false);
        alert("인증이 만료되었습니다. 다시 로그인해주세요.");
        router.push("/");
      } else {
        setIsAuthenticated(false);
      }
    }
  };

  useEffect(() => {
    let interval: NodeJS.Timeout;

    // 첫 실행 시 한 번만 확인
    checkAuthStatus().then(() => {
      // 로그인된 경우에만 일정 간격으로 상태 체크
      if (isAuthenticated) {
        interval = setInterval(checkAuthStatus, 5000);
      }
    });

    return () => {
      if (interval) clearInterval(interval); // 언마운트 시 정리
    };
  }, [isAuthenticated]); // isAuthenticated 변경될 때만 재실행

  // 로그아웃 함수
  const logout = async () => {
    await fetch(`${API_BASE_URL}/api/users/logout`, {
      method: "GET",
      credentials: "include",
    });

    setIsAuthenticated(false); // UI 즉시 변경
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
