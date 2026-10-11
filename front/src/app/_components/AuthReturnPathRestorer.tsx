"use client";

import { usePathname, useRouter } from "next/navigation";

import { useEffect } from "react";

import { useAuth } from "@/lib/auth/AuthProvider";
import { consumeAuthReturnPath } from "@/lib/auth/authReturnPath";

export default function AuthReturnPathRestorer() {
  const pathname = usePathname();
  const router = useRouter();
  const { isLogin, isLoginMemberPending } = useAuth();

  useEffect(() => {
    if (isLoginMemberPending || !isLogin) return;

    const returnPath = consumeAuthReturnPath();
    if (pathname !== "/" || returnPath === "/") return;

    router.replace(returnPath);
  }, [isLogin, isLoginMemberPending, pathname, router]);

  return null;
}
