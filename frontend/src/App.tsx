import { Navigate, Route, Routes, useLocation, useNavigate } from "react-router-dom";
import { Bell, Bot, Building2, ChevronDown, CircleUserRound, FileText, FileUp, KeyRound, LayoutDashboard, LogOut, Menu, Settings2, ShieldCheck, Users } from "lucide-react";
import { useEffect, useMemo, useRef, useState } from "react";

import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Tooltip, TooltipContent, TooltipRoot, TooltipTrigger } from "@/components/ui/tooltip";
import { hasPermission } from "@/lib/access";
import { cn } from "@/lib/utils";
import { AuthProvider, useAuth } from "@/state/auth-store";
import { LandingPage } from "@/pages/landing-page";
import { LoginPage } from "@/pages/login-page";
import { DashboardPage } from "@/pages/dashboard-page";
import { ContractsPage } from "@/pages/contracts-page";
import { ContractDetailPage } from "@/pages/contract-detail-page";
import { ContractUploadPage } from "@/pages/contract-upload-page";
import { PartnersPage } from "@/pages/partners-page";
import { UsersPage } from "@/pages/users-page";
import { NotificationsPage } from "@/pages/notifications-page";
import { ChatPage } from "@/pages/chat-page";
import { SystemSettingsPage } from "@/pages/system-settings-page";
import { AccountPage } from "@/pages/account-page";

type NavItem = {
  label: string;
  path: string;
  icon: typeof LayoutDashboard;
  permission?: string;
};

const navItems: NavItem[] = [
  { label: "Dashboard", path: "/app/dashboard", icon: LayoutDashboard, permission: "DASHBOARD_VIEW" },
  { label: "Upload contract", path: "/app/contracts/upload", icon: FileUp, permission: "CONTRACT_CREATE" },
  { label: "Contracts", path: "/app/contracts", icon: FileText, permission: "CONTRACT_VIEW" },
  { label: "Partners", path: "/app/partners", icon: Building2, permission: "PARTNER_VIEW" },
  { label: "Users", path: "/app/users", icon: Users, permission: "USER_VIEW" },
  { label: "System settings", path: "/app/settings", icon: Settings2, permission: "DOCUMENT_TYPE_MANAGE" },
  { label: "My account", path: "/app/account", icon: CircleUserRound },
  { label: "Notifications", path: "/app/notifications", icon: Bell, permission: "NOTIFICATION_VIEW" },
  { label: "RAG Chat", path: "/app/chat", icon: Bot, permission: "AI_CHAT" },
];

function AppRoutes() {
  const { token, profile, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [collapsed, setCollapsed] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const userMenuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (location.pathname.startsWith("/app") && !token) {
      navigate("/login");
    }
  }, [location.pathname, navigate, token]);

  useEffect(() => {
    setUserMenuOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    function closeUserMenu(event: MouseEvent) {
      if (userMenuRef.current && !userMenuRef.current.contains(event.target as Node)) {
        setUserMenuOpen(false);
      }
    }

    function closeOnEscape(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setUserMenuOpen(false);
      }
    }

    document.addEventListener("mousedown", closeUserMenu);
    document.addEventListener("keydown", closeOnEscape);
    return () => {
      document.removeEventListener("mousedown", closeUserMenu);
      document.removeEventListener("keydown", closeOnEscape);
    };
  }, []);

  const activePath = useMemo(() => {
    return navItems.find((item) => location.pathname.startsWith(item.path))?.path ?? "";
  }, [location.pathname]);
  const visibleNavItems = useMemo(
    () => navItems.filter((item) => !item.permission || hasPermission(profile, item.permission)),
    [profile],
  );

  if (!token && location.pathname.startsWith("/app")) {
    return <Navigate to="/login" replace />;
  }

  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/app/*"
        element={
          <div className="min-h-screen p-3 md:p-5">
            <div className="flex min-h-[calc(100vh-1.5rem)] gap-4">
              <aside
                className={cn(
                  "glass-panel hidden shrink-0 overflow-hidden rounded-[28px] border border-brand-500/30 bg-gradient-to-b from-brand-900 via-brand-800 to-brand-700 px-3 py-4 text-white shadow-[0_24px_60px_rgba(14,55,120,0.24)] transition-all duration-300 md:flex md:flex-col",
                  collapsed ? "w-[92px]" : "w-[292px]",
                )}
              >
                <div className="mb-4 flex items-center justify-between gap-3 px-2">
                  <div className={cn("flex items-center gap-3", collapsed && "justify-center")}>
                    <div className="flex h-12 w-12 items-center justify-center rounded-3xl border border-white/20 bg-white/15 text-white shadow-card">
                      <ShieldCheck className="h-6 w-6" />
                    </div>
                    {!collapsed && (
                      <div>
                        <p className="text-sm font-semibold uppercase tracking-[0.24em] text-white">IDP System</p>
                        <p className="text-xs text-brand-100">Enterprise contract intelligence</p>
                      </div>
                    )}
                  </div>
                  {!collapsed && (
                    <Button className="text-white hover:bg-white/10 hover:text-white" size="icon" variant="ghost" onClick={() => setCollapsed(true)}>
                      <Menu className="h-5 w-5" />
                    </Button>
                  )}
                </div>

                {collapsed && (
                  <Button className="mb-4 self-center text-white hover:bg-white/10 hover:text-white" size="icon" variant="ghost" onClick={() => setCollapsed(false)}>
                    <Menu className="h-5 w-5" />
                  </Button>
                )}

                <Tooltip>
                  <nav className="flex flex-1 flex-col gap-2">
                    {visibleNavItems.map((item) => {
                      const Icon = item.icon;
                      const active = activePath === item.path;
                      const content = (
                        <button
                          key={item.path}
                          onClick={() => navigate(item.path)}
                          className={cn(
                            "group flex items-center gap-3 rounded-3xl px-3 py-3 text-left transition",
                            active
                              ? "bg-white text-brand-800 shadow-[0_12px_30px_rgba(5,34,82,0.2)]"
                              : "text-brand-50 hover:bg-white/10 hover:text-white",
                            collapsed && "justify-center px-2",
                          )}
                        >
                          <Icon className="h-5 w-5 shrink-0" />
                          {!collapsed && <span className="text-sm font-semibold">{item.label}</span>}
                        </button>
                      );

                      return collapsed ? (
                        <TooltipRoot key={item.path}>
                          <TooltipTrigger asChild>{content}</TooltipTrigger>
                          <TooltipContent>{item.label}</TooltipContent>
                        </TooltipRoot>
                      ) : (
                        content
                      );
                    })}
                  </nav>
                </Tooltip>

                <Card className={cn("mt-4 overflow-hidden border-white/15 bg-white/10 text-white", collapsed && "p-3")}>
                  {collapsed ? (
                    <div className="flex justify-center">
                      <Bot className="h-7 w-7 text-brand-200" />
                    </div>
                  ) : (
                    <>
                      <p className="text-xs uppercase tracking-[0.28em] text-brand-200">Assistant</p>
                      <p className="mt-2 text-lg font-semibold">RAG robot is ready</p>
                      <p className="mt-1 text-sm text-brand-50/90">Ask contracts, risks, payment clauses, and expiry details.</p>
                    </>
                  )}
                </Card>
              </aside>

              <main className="flex-1">
                <header className="glass-panel relative z-40 mb-4 flex flex-col gap-3 rounded-[30px] border border-white/60 px-5 py-4 shadow-soft md:flex-row md:items-center md:justify-between">
                  <div>
                    <p className="text-sm uppercase tracking-[0.24em] text-brand-600">Intelligent Document Processing</p>
                    <h1 className="mt-1 text-2xl font-semibold text-slate-900">
                      {navItems.find((item) => item.path === activePath)?.label ?? "Workspace"}
                    </h1>
                  </div>
                  <div className="relative" ref={userMenuRef}>
                    <button
                      type="button"
                      className="flex items-center gap-3 rounded-3xl border border-brand-100 bg-brand-50 px-3 py-2 text-left transition hover:border-brand-200 hover:bg-brand-100/70 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-400"
                      aria-expanded={userMenuOpen}
                      aria-haspopup="menu"
                      onClick={() => setUserMenuOpen((open) => !open)}
                    >
                      <span className="flex h-10 w-10 items-center justify-center rounded-full bg-brand-600 text-sm font-bold text-white shadow-card">
                        {getInitials(profile?.fullName)}
                      </span>
                      <span className="hidden min-w-0 sm:block">
                        <span className="block max-w-44 truncate text-sm font-semibold text-slate-900">{profile?.fullName ?? "Unknown user"}</span>
                        <span className="block text-xs text-slate-600">{profile?.roles.join(", ") || "No role"}</span>
                      </span>
                      <ChevronDown className={cn("h-4 w-4 text-brand-700 transition-transform", userMenuOpen && "rotate-180")} />
                    </button>

                    {userMenuOpen && (
                      <div className="absolute right-0 top-full z-50 mt-3 w-[min(21rem,calc(100vw-2.5rem))] overflow-hidden rounded-[24px] border border-brand-100 bg-white shadow-[0_24px_60px_rgba(15,52,96,0.2)]" role="menu">
                        <div className="bg-gradient-to-r from-brand-800 to-brand-600 p-4 text-white">
                          <div className="flex items-center gap-3">
                            <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full border border-white/25 bg-white/15 text-base font-bold">
                              {getInitials(profile?.fullName)}
                            </span>
                            <div className="min-w-0">
                              <p className="truncate font-semibold">{profile?.fullName ?? "Unknown user"}</p>
                              <p className="truncate text-xs text-brand-50/90">{profile?.email ?? "No email"}</p>
                              <p className="mt-1 text-xs font-medium text-cyan-100">{profile?.roles.join(", ") || "No role"}</p>
                            </div>
                          </div>
                        </div>
                        <div className="p-2">
                          <button className="flex w-full items-center gap-3 rounded-2xl px-3 py-3 text-sm font-medium text-slate-700 hover:bg-brand-50 hover:text-brand-800" role="menuitem" onClick={() => { setUserMenuOpen(false); navigate("/app/account"); }}>
                            <CircleUserRound className="h-5 w-5 text-brand-600" /> Account details
                          </button>
                          <button className="flex w-full items-center gap-3 rounded-2xl px-3 py-3 text-sm font-medium text-slate-700 hover:bg-brand-50 hover:text-brand-800" role="menuitem" onClick={() => { setUserMenuOpen(false); navigate("/app/account#change-password"); }}>
                            <KeyRound className="h-5 w-5 text-brand-600" /> Change password
                          </button>
                          <button className="flex w-full items-center gap-3 rounded-2xl px-3 py-3 text-sm font-medium text-slate-700 hover:bg-brand-50 hover:text-brand-800" role="menuitem" onClick={() => { setUserMenuOpen(false); navigate("/app/notifications"); }}>
                            <Bell className="h-5 w-5 text-brand-600" /> Notifications
                          </button>
                          <div className="my-1 border-t border-slate-100" />
                          <button className="flex w-full items-center gap-3 rounded-2xl px-3 py-3 text-sm font-semibold text-rose-600 hover:bg-rose-50" role="menuitem" onClick={logout}>
                            <LogOut className="h-5 w-5" /> Logout
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                </header>

                <Routes>
                  <Route path="/" element={<Navigate to="/app/dashboard" replace />} />
                  <Route path="/dashboard" element={<PermissionGate permission="DASHBOARD_VIEW"><DashboardPage /></PermissionGate>} />
                  <Route path="/contracts" element={<PermissionGate permission="CONTRACT_VIEW"><ContractsPage /></PermissionGate>} />
                  <Route path="/contracts/upload" element={<PermissionGate permission="CONTRACT_CREATE"><ContractUploadPage /></PermissionGate>} />
                  <Route path="/contracts/:contractId" element={<PermissionGate permission="CONTRACT_VIEW"><ContractDetailPage /></PermissionGate>} />
                  <Route path="/partners" element={<PermissionGate permission="PARTNER_VIEW"><PartnersPage /></PermissionGate>} />
                  <Route path="/users" element={<PermissionGate permission="USER_VIEW"><UsersPage /></PermissionGate>} />
                  <Route path="/settings" element={<PermissionGate permission="DOCUMENT_TYPE_MANAGE"><SystemSettingsPage /></PermissionGate>} />
                  <Route path="/account" element={<AccountPage />} />
                  <Route path="/notifications" element={<PermissionGate permission="NOTIFICATION_VIEW"><NotificationsPage /></PermissionGate>} />
                  <Route path="/chat" element={<PermissionGate permission="AI_CHAT"><ChatPage /></PermissionGate>} />
                </Routes>
              </main>
            </div>
          </div>
        }
      />
    </Routes>
  );
}

function PermissionGate({ children, permission }: { children: React.ReactNode; permission: string }) {
  const { profile } = useAuth();
  if (hasPermission(profile, permission)) {
    return <>{children}</>;
  }
  return (
    <Card className="max-w-2xl border-amber-200 bg-amber-50">
      <ShieldCheck className="h-7 w-7 text-amber-700" />
      <h2 className="mt-4 text-2xl font-semibold text-slate-900">Access restricted</h2>
      <p className="mt-3 text-sm leading-7 text-slate-700">Your account does not have the {permission} permission required for this page.</p>
    </Card>
  );
}

function getInitials(fullName?: string) {
  if (!fullName?.trim()) {
    return "U";
  }
  const words = fullName.trim().split(/\s+/);
  return `${words[0]?.[0] ?? ""}${words.length > 1 ? words[words.length - 1]?.[0] ?? "" : ""}`.toUpperCase();
}

export default function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}
