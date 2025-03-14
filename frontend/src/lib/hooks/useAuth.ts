import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

const API_BASE_URL = "http://localhost:8080"; // Spring Boot 서버 주소

export const useAuth = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [wasAuthenticated, setWasAuthenticated] = useState(false); // 한 번이라도 로그인한 적 있는지 추적
  const router = useRouter();

  // API 응답 처리 함수
  const handleAuthResponse = async (response: Response) => {
    if (response.ok) {
      setIsAuthenticated(true);
      setWasAuthenticated(true); // 로그인한 적 있음
      return;
    }

    if ([403, 401, 500].includes(response.status)) {
      // console.warn("Access Token 만료됨 또는 서버 에러 발생, Refresh 시도");
      const refreshed = await refreshAccessToken();
      if (!refreshed && wasAuthenticated) {
        // console.warn("Refresh Token도 만료됨, 로그아웃 처리");
        handleAuthExpired();
      }
    } else {
      // console.error("인증 확인 중 오류 발생:", response.status);
      if (wasAuthenticated) handleAuthExpired();
    }
  };

  // API 오류 처리 함수
  const handleError = (error: any) => {
    // console.error("API 요청 실패:", error);
    if (wasAuthenticated) handleAuthExpired();
  };

  // 인증 상태 확인 (Access Token 확인)
  const checkAuthStatus = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/users/status`, {
        method: "GET",
        credentials: "include",
      });
      await handleAuthResponse(response);
    } catch (error) {
      handleError(error);
    }
  };

  // Access Token 갱신 요청 (Refresh Token 사용)
  const refreshAccessToken = async (): Promise<boolean> => {
    try {
      const response = await fetch(`${API_BASE_URL}/api/users/refresh`, {
        method: "GET",
        credentials: "include",
      });

      if (response.ok) {
        // console.log("Access Token 재발급 성공, 재검증 시도");
        await checkAuthStatus(); // 토큰이 갱신된 후 다시 인증 상태 확인
        return true;
      } else {
        // console.warn("Refresh Token도 만료됨, 로그아웃 처리");
        return false;
      }
    } catch (error) {
      handleError(error);
      return false;
    }
  };

  // 인증 만료 처리 (로그아웃)
  const handleAuthExpired = () => {
    setIsAuthenticated(false);
    alert("인증이 만료되었습니다. 다시 로그인해주세요.");
    router.push("/");
  };

  // 자동 인증 체크 설정
  useEffect(() => {
    let interval: NodeJS.Timeout;

    checkAuthStatus().then(() => {
      if (isAuthenticated) {
        interval = setInterval(checkAuthStatus, 5000);
      }
    });

    return () => clearInterval(interval);
  }, [isAuthenticated]);

  // 로그아웃 함수
  const logout = async () => {
    await fetch(`${API_BASE_URL}/api/users/logout`, {
      method: "GET",
      credentials: "include",
    });

    setIsAuthenticated(false);
    setWasAuthenticated(false);
    router.push("/");
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
    setWasAuthenticated,
  };
};
