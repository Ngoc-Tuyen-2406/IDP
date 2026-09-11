import type { LoginResponse } from "@/lib/api";

type AccessProfile = Pick<LoginResponse["user"], "permissions" | "roles"> | null | undefined;

export function hasPermission(profile: AccessProfile, permission: string) {
  const expected = permission.toUpperCase();
  return profile?.permissions.some((item) => item.toUpperCase() === expected) ?? false;
}

export function hasAnyPermission(profile: AccessProfile, permissions: string[]) {
  return permissions.some((permission) => hasPermission(profile, permission));
}

export function hasRole(profile: AccessProfile, role: string) {
  const expected = role.toUpperCase();
  return profile?.roles.some((item) => item.toUpperCase() === expected) ?? false;
}
