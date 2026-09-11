import { Check, CheckCheck, Settings2, Trash2 } from "lucide-react";
import { useEffect, useState } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { apiRequest, type NotificationItem, type NotificationSettings, type PageResponse } from "@/lib/api";
import { formatDate } from "@/lib/format";
import { useAuth } from "@/state/auth-store";

export function NotificationsPage() {
  const { token } = useAuth();
  const [notifications, setNotifications] = useState<PageResponse<NotificationItem> | null>(null);
  const [settings, setSettings] = useState<NotificationSettings | null>(null);
  const [showSettings, setShowSettings] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function loadNotifications() {
    if (!token) {
      return;
    }
    const [data, preferences] = await Promise.all([
      apiRequest<PageResponse<NotificationItem>>("/api/v1/notifications?size=100", {}, token),
      apiRequest<NotificationSettings>("/api/v1/notification-settings", {}, token),
    ]);
    setNotifications(data);
    setSettings(preferences);
  }

  useEffect(() => {
    loadNotifications().catch((reason: unknown) => setError(reason instanceof Error ? reason.message : "Unable to load notifications."));
  }, [token]);

  async function markAllRead() {
    if (!token) {
      return;
    }
    await apiRequest<void>(
      "/api/v1/notifications/read-all",
      {
        method: "PUT",
        body: JSON.stringify({}),
      },
      token,
    );
    await loadNotifications();
  }

  async function markRead(notificationId: number) {
    if (!token) {
      return;
    }
    try {
      await apiRequest<void>(`/api/v1/notifications/${notificationId}/read`, { method: "PUT", body: JSON.stringify({}) }, token);
      await loadNotifications();
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to mark notification as read.");
    }
  }

  async function deleteNotification(notificationId: number) {
    if (!token || !window.confirm("Delete this notification?")) {
      return;
    }
    try {
      await apiRequest<void>(`/api/v1/notifications/${notificationId}`, { method: "DELETE" }, token);
      await loadNotifications();
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to delete notification.");
    }
  }

  async function updateSetting(key: Exclude<keyof NotificationSettings, "userId" | "updatedAt">, value: boolean) {
    if (!token || !settings) {
      return;
    }
    try {
      const updated = await apiRequest<NotificationSettings>("/api/v1/notification-settings", {
        method: "PUT",
        body: JSON.stringify({ [key]: value }),
      }, token);
      setSettings(updated);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to update notification preference.");
    }
  }

  return (
    <div className="space-y-4">
      <Card className="flex flex-col gap-4 border-brand-100 bg-gradient-to-r from-brand-50 to-cyan-50 md:flex-row md:items-center md:justify-between">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.24em] text-brand-700">Review activity</p>
          <h2 className="mt-2 text-2xl font-semibold text-slate-900">Activity across review and approval flows</h2>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button variant="outline" onClick={() => setShowSettings((current) => !current)}><Settings2 className="h-4 w-4" />Preferences</Button>
          <Button variant="outline" onClick={markAllRead}><CheckCheck className="h-4 w-4" />Mark all as read</Button>
        </div>
      </Card>

      {error && <Card className="border-rose-200 bg-rose-50 text-rose-700">{error}</Card>}

      {showSettings && settings && (
        <Card>
          <p className="text-sm uppercase tracking-[0.24em] text-brand-600">Preferences</p>
          <h3 className="mt-2 text-xl font-semibold text-slate-900">Choose what you receive</h3>
          <div className="mt-5 grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
            {([
              ["emailEnabled", "Email delivery"],
              ["systemNotification", "System notifications"],
              ["contractNew", "New contracts"],
              ["contractApproval", "Approval activity"],
              ["contractExpiring", "Expiring contracts"],
              ["commentMention", "Comment mentions"],
            ] as const).map(([key, label]) => (
              <label key={key} className="flex cursor-pointer items-center justify-between gap-3 rounded-[22px] border border-slate-100 bg-slate-50/70 px-4 py-4 text-sm font-medium text-slate-800">
                {label}
                <input type="checkbox" checked={settings[key]} onChange={(event) => updateSetting(key, event.target.checked)} />
              </label>
            ))}
          </div>
        </Card>
      )}

      <div className="grid gap-4">
        {notifications?.content.map((notification) => (
          <Card key={notification.notificationId}>
            <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
              <div>
                <div className="flex items-center gap-3">
                  <h3 className="text-lg font-semibold text-slate-900">{notification.title}</h3>
                  <Badge tone={notification.isRead ? "neutral" : "info"}>{notification.isRead ? "Read" : "New"}</Badge>
                </div>
                <p className="mt-3 text-sm leading-7 text-slate-600">{notification.content}</p>
              </div>
              <p className="text-sm text-slate-500">{formatDate(notification.createdAt)}</p>
            </div>
            <div className="mt-4 flex flex-wrap gap-2">
              {!notification.isRead && <Button size="sm" variant="outline" onClick={() => markRead(notification.notificationId)}><Check className="h-4 w-4" />Mark read</Button>}
              <Button size="sm" variant="ghost" className="text-rose-600 hover:bg-rose-50 hover:text-rose-700" onClick={() => deleteNotification(notification.notificationId)}><Trash2 className="h-4 w-4" />Delete</Button>
            </div>
          </Card>
        ))}
        {notifications?.content.length === 0 && <Card className="text-center text-sm text-slate-500">No notifications yet.</Card>}
      </div>
    </div>
  );
}
