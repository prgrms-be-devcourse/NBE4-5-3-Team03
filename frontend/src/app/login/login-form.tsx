"use client";

import client from "@/lib/backend/client";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export function LoginForm({
  className,
  ...props
}: React.ComponentProps<"div">) {
  async function login(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const form = e.target as HTMLFormElement;
    const data = {
      username: form.username.value,
      password: form.password.value,
    };
    const response = await client.POST("/api/users/login", {
      body: data,
      credentials: "include",
    });
    if (response.error) {
      alert("아이디 또는 비밀번호가 일치하지 않습니다.");
      return;
    }
    // show success message and redirect to main page
    // alert(response.data)
    // alert(`${response.data.username}`+"님 환영합니다.")
    window.location.href = "/";
  }

  return (
    <div className={cn("flex flex-col gap-6", className)} {...props}>
      <Card>
        <CardHeader>
          <CardTitle>로그인</CardTitle>
          <CardDescription>
            로그인을 위해 ID와 비밀번호를 입력해주세요.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={login}>
            <div className="flex flex-col gap-6">
              <div className="grid gap-3">
                <Label htmlFor="username">유저 ID</Label>
                <Input
                  id="username"
                  type="username"
                  placeholder="유저 ID"
                  required
                />
              </div>
              <div className="grid gap-3">
                <div className="flex items-center">
                  <Label htmlFor="password">비밀번호</Label>
                </div>
                <Input
                  id="password"
                  type="password"
                  placeholder="비밀번호"
                  required
                />
              </div>
              <div className="flex flex-col gap-3">
                <Button type="submit" className="w-full">
                  로그인
                </Button>
              </div>
            </div>
            <div className="mt-4 text-center text-sm">
              계정이 없으신가요?{" "}
              <a href="/signup" className="underline underline-offset-4">
                회원가입
              </a>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
