import { AlertCircle, LockKeyhole, Mail } from "lucide-react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useAuth } from "@/state/auth-store";

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await login({ email, password });
      navigate("/app/dashboard");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Dang nhap that bai");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center px-4 py-8">
      <div className="grid w-full max-w-6xl gap-6 lg:grid-cols-[1fr_0.9fr]">
        <Card className="relative overflow-hidden bg-gradient-to-br from-brand-700 via-brand-600 to-brand-900 p-10 text-white">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(255,255,255,0.26),transparent_28%)]" />
          <div className="relative">
            <p className="text-sm uppercase tracking-[0.34em] text-brand-100">Welcome back</p>
            <h1 className="mt-4 text-5xl font-semibold leading-tight">Operate contract workflows with AI clarity.</h1>
            <p className="mt-6 max-w-xl text-base leading-8 text-brand-50/80">
              Login to access dashboard analytics, contract processing, approval steps, metadata validation, and RAG chat from one workspace.
            </p>
            <div className="mt-10 grid gap-4 sm:grid-cols-2">
              {[
                "JWT auth and session control",
                "Contract upload and tracking",
                "AI summary and risk analysis",
                "Comments, notifications, and approvals",
              ].map((item) => (
                <div key={item} className="rounded-[24px] border border-white/15 bg-white/10 px-4 py-4 backdrop-blur">
                  {item}
                </div>
              ))}
            </div>
          </div>
        </Card>

        <Card className="p-8 md:p-10">
          <p className="text-sm uppercase tracking-[0.3em] text-brand-600">Login</p>
          <h2 className="mt-2 text-3xl font-semibold text-slate-900">Sign in to IDP</h2>
          <p className="mt-3 text-sm leading-7 text-slate-500">Use a real account from the backend database. This form posts directly to `/api/v1/auth/login`.</p>

          <form className="mt-8 space-y-4" onSubmit={handleSubmit}>
            <label className="block">
              <span className="mb-2 flex items-center gap-2 text-sm font-medium text-slate-700">
                <Mail className="h-4 w-4" />
                Email
              </span>
              <Input value={email} onChange={(event) => setEmail(event.target.value)} placeholder="admin@company.com" />
            </label>
            <label className="block">
              <span className="mb-2 flex items-center gap-2 text-sm font-medium text-slate-700">
                <LockKeyhole className="h-4 w-4" />
                Password
              </span>
              <Input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                placeholder="Enter your password"
              />
            </label>

            {error && (
              <div className="rounded-3xl bg-rose-50 px-4 py-3 text-sm text-rose-700">
                <div className="flex items-center gap-2">
                  <AlertCircle className="h-4 w-4" />
                  {error}
                </div>
              </div>
            )}

            <Button className="w-full" disabled={loading}>
              {loading ? "Signing in..." : "Dang nhap"}
            </Button>
          </form>
        </Card>
      </div>
    </div>
  );
}
