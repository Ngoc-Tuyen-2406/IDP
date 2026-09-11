import { Building2, FileType2, KeyRound, LockKeyhole, Plus, Save, ShieldCheck, Trash2 } from "lucide-react";
import { useEffect, useState, type FormEvent } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { hasAnyPermission, hasPermission } from "@/lib/access";
import {
  apiRequest,
  type DepartmentItem,
  type DocumentTypeItem,
  type PermissionItem,
  type RoleDetail,
  type RoleItem,
} from "@/lib/api";
import { useAuth } from "@/state/auth-store";

type Tab = "departments" | "documentTypes" | "roles" | "permissions";
type SettingRecord = { id: number; name: string; description: string };
type EditorState = { id: number | null; name: string; description: string };

const emptyEditor: EditorState = { id: null, name: "", description: "" };

const tabs: Array<{ id: Tab; label: string; icon: typeof Building2; adminOnly?: boolean }> = [
  { id: "departments", label: "Departments", icon: Building2 },
  { id: "documentTypes", label: "Document types", icon: FileType2 },
  { id: "roles", label: "Roles", icon: ShieldCheck, adminOnly: true },
  { id: "permissions", label: "Permissions", icon: KeyRound, adminOnly: true },
];

const endpoints: Record<Tab, string> = {
  departments: "/api/v1/departments",
  documentTypes: "/api/v1/document-types",
  roles: "/api/v1/roles",
  permissions: "/api/v1/permissions",
};

export function SystemSettingsPage() {
  const { profile, token } = useAuth();
  const [tab, setTab] = useState<Tab>("departments");
  const [departments, setDepartments] = useState<DepartmentItem[]>([]);
  const [documentTypes, setDocumentTypes] = useState<DocumentTypeItem[]>([]);
  const [roles, setRoles] = useState<RoleItem[]>([]);
  const [permissions, setPermissions] = useState<PermissionItem[]>([]);
  const [editor, setEditor] = useState<EditorState>(emptyEditor);
  const [selectedRoleId, setSelectedRoleId] = useState<number | null>(null);
  const [roleDetail, setRoleDetail] = useState<RoleDetail | null>(null);
  const [permissionIds, setPermissionIds] = useState<number[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isAdmin = hasPermission(profile, "ROLE_MANAGE") && hasPermission(profile, "PERMISSION_MANAGE");
  const canManage = hasAnyPermission(profile, ["DEPARTMENT_MANAGE", "DOCUMENT_TYPE_MANAGE", "ROLE_VIEW", "PERMISSION_VIEW"]);
  const adminTab = tab === "roles" || tab === "permissions";
  const canManageTab = tab === "departments"
    ? hasPermission(profile, "DEPARTMENT_MANAGE")
    : tab === "documentTypes"
      ? hasPermission(profile, "DOCUMENT_TYPE_MANAGE")
      : tab === "roles"
        ? hasPermission(profile, "ROLE_MANAGE")
        : hasPermission(profile, "PERMISSION_MANAGE");

  function recordsFor(activeTab: Tab): SettingRecord[] {
    if (activeTab === "departments") {
      return departments.map((item) => ({ id: item.departmentId, name: item.departmentName, description: item.description ?? "" }));
    }
    if (activeTab === "documentTypes") {
      return documentTypes.map((item) => ({ id: item.documentTypeId, name: item.name, description: item.description ?? "" }));
    }
    if (activeTab === "roles") {
      return roles.map((item) => ({ id: item.roleId, name: item.roleName, description: item.description ?? "" }));
    }
    return permissions.map((item) => ({ id: item.permissionId, name: item.permissionName, description: item.description ?? "" }));
  }

  function bodyFor(activeTab: Tab) {
    const description = editor.description.trim() || null;
    if (activeTab === "departments") {
      return { departmentName: editor.name.trim(), description };
    }
    if (activeTab === "documentTypes") {
      return { name: editor.name.trim(), description };
    }
    if (activeTab === "roles") {
      return { roleName: editor.name.trim(), description };
    }
    return { permissionName: editor.name.trim(), description };
  }

  async function loadData() {
    if (!token || !canManage) {
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      const [departmentData, documentTypeData, roleData, permissionData] = await Promise.all([
        apiRequest<DepartmentItem[]>("/api/v1/departments", {}, token),
        apiRequest<DocumentTypeItem[]>("/api/v1/document-types", {}, token),
        hasPermission(profile, "ROLE_VIEW") ? apiRequest<RoleItem[]>("/api/v1/roles", {}, token) : Promise.resolve([]),
        hasPermission(profile, "PERMISSION_VIEW") ? apiRequest<PermissionItem[]>("/api/v1/permissions", {}, token) : Promise.resolve([]),
      ]);
      setDepartments(departmentData);
      setDocumentTypes(documentTypeData);
      setRoles(roleData);
      setPermissions(permissionData);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to load system settings.");
    } finally {
      setLoading(false);
    }
  }

  async function loadRoleDetail(roleId: number) {
    if (!token) {
      return;
    }
    try {
      const detail = await apiRequest<RoleDetail>(`/api/v1/roles/${roleId}`, {}, token);
      setRoleDetail(detail);
      setPermissionIds(detail.permissions.map((permission) => permission.permissionId));
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to load role permissions.");
    }
  }

  useEffect(() => {
    loadData().catch(() => undefined);
  }, [canManage, profile, token]);

  useEffect(() => {
    setEditor(emptyEditor);
  }, [tab]);

  useEffect(() => {
    if (selectedRoleId && isAdmin) {
      loadRoleDetail(selectedRoleId).catch(() => undefined);
    } else {
      setRoleDetail(null);
      setPermissionIds([]);
    }
  }, [isAdmin, selectedRoleId, token]);

  async function saveRecord(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!token || !editor.name.trim() || !canManageTab) {
      return;
    }
    setSaving(true);
    setError(null);
    try {
      await apiRequest(
        editor.id == null ? endpoints[tab] : `${endpoints[tab]}/${editor.id}`,
        { method: editor.id == null ? "POST" : "PUT", body: JSON.stringify(bodyFor(tab)) },
        token,
      );
      setEditor(emptyEditor);
      await loadData();
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to save the record.");
    } finally {
      setSaving(false);
    }
  }

  async function deleteRecord(record: SettingRecord) {
    if (!token || !canManageTab || !window.confirm("Delete this record? Referenced data cannot be removed.")) {
      return;
    }
    try {
      await apiRequest<void>(`${endpoints[tab]}/${record.id}`, { method: "DELETE" }, token);
      if (tab === "roles" && selectedRoleId === record.id) {
        setSelectedRoleId(null);
      }
      setEditor(emptyEditor);
      await loadData();
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to delete the record.");
    }
  }

  function togglePermission(permissionId: number) {
    setPermissionIds((current) => current.includes(permissionId)
      ? current.filter((id) => id !== permissionId)
      : [...current, permissionId]);
  }

  async function savePermissionMatrix() {
    if (!token || !selectedRoleId || !roleDetail) {
      return;
    }
    const existing = new Set(roleDetail.permissions.map((permission) => permission.permissionId));
    const selected = new Set(permissionIds);
    const added = [...selected].filter((id) => !existing.has(id));
    const removed = [...existing].filter((id) => !selected.has(id));
    setSaving(true);
    setError(null);
    try {
      if (added.length > 0) {
        await apiRequest(`/api/v1/roles/${selectedRoleId}/permissions`, {
          method: "POST",
          body: JSON.stringify({ permissionIds: added }),
        }, token);
      }
      await Promise.all(removed.map((permissionId) => apiRequest<void>(
        `/api/v1/roles/${selectedRoleId}/permissions/${permissionId}`,
        { method: "DELETE" },
        token,
      )));
      await loadRoleDetail(selectedRoleId);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Unable to update the permission matrix.");
    } finally {
      setSaving(false);
    }
  }

  if (!canManage) {
    return (
      <Card className="max-w-2xl">
        <LockKeyhole className="h-7 w-7 text-brand-600" />
        <h2 className="mt-4 text-2xl font-semibold text-slate-900">System settings are restricted</h2>
        <p className="mt-3 text-sm leading-7 text-slate-600">Only administrators and managers can maintain system reference data. The API also enforces this permission independently.</p>
      </Card>
    );
  }

  const records = recordsFor(tab);
  const selectedTab = tabs.find((item) => item.id === tab);

  return (
    <div className="space-y-4">
      <Card>
        <p className="text-sm uppercase tracking-[0.24em] text-brand-600">System settings</p>
        <h2 className="mt-2 text-2xl font-semibold text-slate-900">Organization data and access control</h2>
        <p className="mt-2 max-w-3xl text-sm leading-7 text-slate-600">Maintain the reference data used by contract upload and the role-based access model used throughout the platform.</p>
        <div className="mt-6 flex flex-wrap gap-2">
          {tabs.filter((item) => !item.adminOnly || isAdmin).map((item) => {
            const Icon = item.icon;
            return <Button key={item.id} size="sm" variant={tab === item.id ? "default" : "outline"} onClick={() => setTab(item.id)}><Icon className="h-4 w-4" />{item.label}</Button>;
          })}
        </div>
      </Card>

      {error && <Card className="border-rose-200 bg-rose-50 text-rose-700">{error}</Card>}

      <div className="grid gap-4 xl:grid-cols-[0.82fr_1.18fr]">
        {canManageTab ? (
          <Card>
            <div className="flex items-center gap-3">
              <Plus className="h-5 w-5 text-brand-600" />
              <div><h3 className="text-lg font-semibold text-slate-900">{editor.id == null ? `Create ${selectedTab?.label ?? "record"}` : `Edit ${selectedTab?.label ?? "record"}`}</h3><p className="mt-1 text-sm text-slate-500">Changes are saved through the protected API.</p></div>
            </div>
            <form className="mt-5 space-y-4" onSubmit={saveRecord}>
              <label><span className="mb-2 block text-sm font-medium text-slate-700">Name</span><Input required value={editor.name} onChange={(event) => setEditor((current) => ({ ...current, name: event.target.value }))} /></label>
              <label><span className="mb-2 block text-sm font-medium text-slate-700">Description</span><Textarea className="min-h-[112px]" value={editor.description} onChange={(event) => setEditor((current) => ({ ...current, description: event.target.value }))} /></label>
              <div className="flex gap-3"><Button disabled={saving} type="submit"><Save className="h-4 w-4" />{saving ? "Saving..." : editor.id == null ? "Create" : "Save changes"}</Button>{editor.id != null && <Button type="button" variant="outline" onClick={() => setEditor(emptyEditor)}>Cancel</Button>}</div>
            </form>
          </Card>
        ) : (
          <Card className="border-slate-200 bg-slate-50">
            <LockKeyhole className="h-7 w-7 text-slate-500" />
            <h3 className="mt-4 text-lg font-semibold text-slate-900">View-only access</h3>
            <p className="mt-2 text-sm leading-7 text-slate-600">You can view these records, but your role cannot create, edit, or delete them.</p>
          </Card>
        )}

        <Card>
          <div className="flex items-center justify-between gap-4"><div><h3 className="text-lg font-semibold text-slate-900">{selectedTab?.label}</h3><p className="mt-1 text-sm text-slate-500">{loading ? "Loading..." : `${records.length} records`}</p></div>{adminTab && <Badge tone="info">Admin only</Badge>}</div>
          <div className="mt-5 space-y-3">
            {records.map((record) => (
              <div key={record.id} className="flex flex-col gap-3 rounded-[24px] border border-slate-100 bg-slate-50/70 p-4 md:flex-row md:items-center md:justify-between">
                <div><p className="font-semibold text-slate-900">{record.name}</p><p className="mt-1 text-sm leading-6 text-slate-500">{record.description || "No description"}</p></div>
                <div className="flex gap-2">
                  {tab === "roles" && isAdmin && <Button size="sm" variant="outline" onClick={() => setSelectedRoleId(record.id)}>Permissions</Button>}
                  {canManageTab && <Button size="sm" variant="outline" onClick={() => setEditor(record)}>Edit</Button>}
                  {canManageTab && <Button size="sm" variant="ghost" className="text-rose-600 hover:bg-rose-50 hover:text-rose-700" onClick={() => deleteRecord(record)}><Trash2 className="h-4 w-4" />Delete</Button>}
                  {!canManageTab && <span className="text-xs font-medium text-slate-500">View only</span>}
                </div>
              </div>
            ))}
            {!loading && records.length === 0 && <p className="rounded-[22px] border border-dashed border-slate-200 px-4 py-8 text-center text-sm text-slate-500">No records yet.</p>}
          </div>
        </Card>
      </div>

      {isAdmin && tab === "roles" && (
        <Card>
          <div className="flex flex-col gap-3 md:flex-row md:items-end md:justify-between"><div><p className="text-sm uppercase tracking-[0.24em] text-brand-600">Permission matrix</p><h3 className="mt-2 text-xl font-semibold text-slate-900">{roleDetail?.roleName ?? "Select a role"}</h3></div><select className="h-11 min-w-56 rounded-2xl border border-slate-200 bg-white px-4 text-sm text-slate-900 outline-none focus:border-brand-400 focus:ring-4 focus:ring-brand-100" value={selectedRoleId ?? ""} onChange={(event) => setSelectedRoleId(event.target.value ? Number(event.target.value) : null)}><option value="">Select role</option>{roles.map((role) => <option key={role.roleId} value={role.roleId}>{role.roleName}</option>)}</select></div>
          {selectedRoleId && <><div className="mt-5 grid gap-3 sm:grid-cols-2 xl:grid-cols-3">{permissions.map((permission) => <label key={permission.permissionId} className="flex cursor-pointer items-start gap-3 rounded-[22px] border border-slate-100 bg-slate-50/70 p-4"><input className="mt-1" type="checkbox" checked={permissionIds.includes(permission.permissionId)} onChange={() => togglePermission(permission.permissionId)} /><span><span className="block text-sm font-semibold text-slate-900">{permission.permissionName}</span><span className="mt-1 block text-xs leading-5 text-slate-500">{permission.description || "No description"}</span></span></label>)}</div><Button className="mt-5" disabled={saving} onClick={savePermissionMatrix}><Save className="h-4 w-4" />Save permission matrix</Button></>}
        </Card>
      )}
    </div>
  );
}
