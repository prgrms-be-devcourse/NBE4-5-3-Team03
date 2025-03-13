import { useEffect, useState } from "react";

const API_BASE_URL = "http://localhost:8080"; // Spring Boot 서버 주소

export const useAuth = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
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

    checkAuthStatus();
  }, []);

  const logout = async () => {
    await fetch(`${API_BASE_URL}/api/users/logout`, {
      method: "GET",
      credentials: "include",
    });
    setIsAuthenticated(false);
  };

  return { isAuthenticated, logout };
};
