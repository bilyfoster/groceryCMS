import { apiFetch, buildUrl } from "@/lib/api";
import type { UserDto } from "@/types/api";

export async function login(email: string, password: string): Promise<UserDto> {
  return apiFetch<UserDto>("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export async function requestMagicLink(email: string): Promise<void> {
  await apiFetch<null>("/auth/magic-link", {
    method: "POST",
    body: JSON.stringify({ email }),
  });
}

export async function verifyMagicLink(token: string): Promise<UserDto | null> {
  const url = buildUrl("/auth/verify", { token });
  const response = await fetch(url, {
    credentials: "include",
    headers: {
      Accept: "application/json",
      "X-Requested-With": "XMLHttpRequest",
      "X-Tenant-Slug": process.env.NEXT_PUBLIC_TENANT_SLUG || "demo",
    },
  });
  const envelope = await response.json();
  if (!response.ok || !envelope.success) {
    throw new Error(envelope.message || "Verification failed");
  }
  return envelope.data;
}

export async function logout(): Promise<void> {
  await apiFetch<null>("/auth/logout", { method: "POST" });
}

export async function getCurrentUser(): Promise<UserDto | null> {
  try {
    return await apiFetch<UserDto>("/auth/me");
  } catch {
    return null;
  }
}

export function isEditorOrAdmin(role: string | undefined): boolean {
  return role === "admin" || role === "editor";
}

export function isAdmin(role: string | undefined): boolean {
  return role === "admin";
}
