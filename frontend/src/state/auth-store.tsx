import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type PropsWithChildren,
} from "react";

import { apiRequest, type LoginResponse } from "@/lib/api";

type AuthState = {
  token: string | null;
  refreshToken: string | null;
  accessTokenExpiresAt: number | null;
  profile: LoginResponse["user"] | null;
};

type LoginInput = {
  email: string;
  password: string;
};

type AuthContextValue = AuthState & {
  login: (input: LoginInput) => Promise<void>;
  refresh: () => Promise<void>;
  logout: () => void;
};

const STORAGE_KEY = "idp-auth";

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [state, setState] = useState<AuthState>(() => {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw
      ? { token: null, refreshToken: null, accessTokenExpiresAt: null, profile: null, ...(JSON.parse(raw) as Partial<AuthState>) }
      : { token: null, refreshToken: null, accessTokenExpiresAt: null, profile: null };
  });

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
  }, [state]);

  useEffect(() => {
    if (!state.refreshToken || !state.accessTokenExpiresAt) {
      return;
    }
    const delay = Math.max(1_000, state.accessTokenExpiresAt - Date.now() - 60_000);
    const timer = window.setTimeout(() => {
      apiRequest<LoginResponse>("/api/v1/auth/refresh-token", {
        method: "POST",
        body: JSON.stringify({ refreshToken: state.refreshToken }),
      })
        .then((data) => setState({
          token: data.accessToken,
          refreshToken: data.refreshToken,
          accessTokenExpiresAt: Date.now() + data.expiresInSeconds * 1_000,
          profile: data.user,
        }))
        .catch(() => setState({ token: null, refreshToken: null, accessTokenExpiresAt: null, profile: null }));
    }, delay);
    return () => window.clearTimeout(timer);
  }, [state.accessTokenExpiresAt, state.refreshToken]);

  const value = useMemo<AuthContextValue>(
    () => ({
      ...state,
      async login(input) {
        const data = await apiRequest<LoginResponse>("/api/v1/auth/login", {
          method: "POST",
          body: JSON.stringify(input),
        });
        setState({
          token: data.accessToken,
          refreshToken: data.refreshToken,
          accessTokenExpiresAt: Date.now() + data.expiresInSeconds * 1_000,
          profile: data.user,
        });
      },
      async refresh() {
        if (!state.refreshToken) {
          throw new Error("No refresh token is available.");
        }
        const data = await apiRequest<LoginResponse>("/api/v1/auth/refresh-token", {
          method: "POST",
          body: JSON.stringify({ refreshToken: state.refreshToken }),
        });
        setState({
          token: data.accessToken,
          refreshToken: data.refreshToken,
          accessTokenExpiresAt: Date.now() + data.expiresInSeconds * 1_000,
          profile: data.user,
        });
      },
      logout() {
        if (state.token && state.refreshToken) {
          apiRequest<void>("/api/v1/auth/logout", {
            method: "POST",
            body: JSON.stringify({ refreshToken: state.refreshToken }),
          }, state.token).catch(() => undefined);
        }
        setState({ token: null, refreshToken: null, accessTokenExpiresAt: null, profile: null });
      },
    }),
    [state],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
