import { KeyRound, Laptop, ShieldCheck, Trash2 } from "lucide-react";
import { useEffect, useState, type FormEvent } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { apiRequest, type PageResponse, type UserProfile, type UserSession } from "@/lib/api";
import { formatDate } from "@/lib/format";
import { useAuth } from "@/state/auth-store";

export function AccountPage() {
  const { token } = useAuth();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [sessions, setSessions] = useState<PageResponse<UserSession> | null>(null);
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function loadAccount() {
    if (!token) {
      return;
    }
    const [profileData, sessionData] = await Promise.all([
      apiRequest<UserProfile>("/api/v1/auth/profile", {}, token),
      apiRequest<PageResponse<UserSession>>("/api/v1/auth/sessions?size=100", {}, token),
    ]);
    setProfile(profileData);
    setSessions(sessionData);
  }

  useEffect(() => {
    loadAccount().catch((reason: unknown) => setError(reason instanceof Error ? reason.message : "Unable to load account details."));
  }, [token]);

  async function changePassword(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!token) {
      return;
    }
    if (newPassword.length < 8) {
      setError("New password must contain at least 8 characters.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setError("New password confirmation does not match.");
      return;
    }
    setSaving(true);
    setError(null);
    setMessage(null);
    try {
      await apiRequest<void>("/api/v1/auth/change-password", {
        method: "PUT",
        body: JSON.stringify({ currentPassword, newPassword, confirmPassword }),
      }, token);
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
      setMessage("Password changed successfully. Existing session rules remain enforced by the backend.");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to change password.");
    } finally {
      setSaving(false);
    }
  }

  async function revokeSession(sessionId: number) {
    if (!token || !window.confirm("Revoke this login session?")) {
      return;
    }
    try {
      await apiRequest<void>(`/api/v1/auth/sessions/${sessionId}`, { method: "DELETE" }, token);
      await loadAccount();
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to revoke session.");
    }
  }

  return (
    <div className="space-y-4">
      {error && <Card className="border-rose-200 bg-rose-50 text-rose-700">{error}</Card>}
      {message && <Card className="border-emerald-200 bg-emerald-50 text-emerald-700">{message}</Card>}

      <Card className="relative overflow-hidden border-brand-500/20 bg-gradient-to-br from-brand-900 via-brand-700 to-cyan-600 text-white">
        <div className="pointer-events-none absolute -right-20 -top-24 h-64 w-64 rounded-full bg-cyan-300/20 blur-2xl" />
        <p className="relative text-sm font-semibold uppercase tracking-[0.24em] text-cyan-100">My account</p>
        <div className="mt-5 flex flex-col gap-5 md:flex-row md:items-center md:justify-between">
          <div className="relative"><h2 className="text-3xl font-semibold text-white">{profile?.fullName ?? "Loading profile"}</h2><p className="mt-2 font-medium text-white/90">{profile?.email}</p></div>
          <div className="relative flex flex-wrap gap-2">{profile?.roles.map((role) => <Badge key={role} className="border border-white/25 bg-white/15 text-white">{role}</Badge>)}</div>
        </div>
        <div className="relative mt-7 grid gap-3 text-sm sm:grid-cols-3">
          <div className="rounded-[22px] border border-cyan-200/40 bg-cyan-300/15 p-4"><p className="font-medium text-cyan-100">Department</p><p className="mt-2 text-base font-bold text-white">{profile?.departmentName || "Unassigned"}</p></div>
          <div className="rounded-[22px] border border-emerald-200/40 bg-emerald-300/15 p-4"><p className="font-medium text-emerald-100">Status</p><p className="mt-2 text-base font-bold text-white">{profile?.status || "Unknown"}</p></div>
          <div className="rounded-[22px] border border-violet-200/40 bg-violet-300/15 p-4"><p className="font-medium text-violet-100">Verified email</p><p className="mt-2 text-base font-bold text-white">{profile?.emailVerified ? "Verified" : "Pending"}</p></div>
        </div>
      </Card>

      <div className="grid gap-4 xl:grid-cols-[0.82fr_1.18fr]">
        <Card id="change-password">
          <div className="flex items-center gap-3"><KeyRound className="h-5 w-5 text-brand-600" /><div><h3 className="text-lg font-semibold text-slate-900">Change password</h3><p className="mt-1 text-sm text-slate-500">Use a new password of at least 8 characters.</p></div></div>
          <form className="mt-5 space-y-4" onSubmit={changePassword}>
            <label><span className="mb-2 block text-sm font-medium text-slate-700">Current password</span><Input required type="password" value={currentPassword} onChange={(event) => setCurrentPassword(event.target.value)} /></label>
            <label><span className="mb-2 block text-sm font-medium text-slate-700">New password</span><Input required minLength={8} type="password" value={newPassword} onChange={(event) => setNewPassword(event.target.value)} /></label>
            <label><span className="mb-2 block text-sm font-medium text-slate-700">Confirm new password</span><Input required minLength={8} type="password" value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} /></label>
            <Button disabled={saving} type="submit"><ShieldCheck className="h-4 w-4" />{saving ? "Changing..." : "Change password"}</Button>
          </form>
        </Card>

        <Card>
          <div className="flex items-center gap-3"><Laptop className="h-5 w-5 text-brand-600" /><div><h3 className="text-lg font-semibold text-slate-900">Active sessions</h3><p className="mt-1 text-sm text-slate-500">Revoke a device session you no longer trust.</p></div></div>
          <div className="mt-5 space-y-3">
            {sessions?.content.map((session) => <div key={session.sessionId} className="flex flex-col gap-3 rounded-[24px] border border-slate-100 bg-slate-50/70 p-4 md:flex-row md:items-center md:justify-between"><div><p className="font-semibold text-slate-900">{session.deviceName || "Unknown device"}</p><p className="mt-1 text-sm text-slate-500">{session.ipAddress || "Unknown IP"} · Created {formatDate(session.createdAt)}</p><p className="mt-1 text-xs text-slate-400">Expires {formatDate(session.expiresAt)}</p></div><div className="flex items-center gap-3"><Badge tone={session.revoked || session.expired ? "neutral" : "success"}>{session.revoked ? "Revoked" : session.expired ? "Expired" : "Active"}</Badge>{!session.revoked && !session.expired && <Button size="sm" variant="ghost" className="text-rose-600 hover:bg-rose-50 hover:text-rose-700" onClick={() => revokeSession(session.sessionId)}><Trash2 className="h-4 w-4" />Revoke</Button>}</div></div>)}
            {sessions?.content.length === 0 && <p className="rounded-[22px] border border-dashed border-slate-200 px-4 py-8 text-center text-sm text-slate-500">No sessions found.</p>}
          </div>
        </Card>
      </div>
    </div>
  );
}
