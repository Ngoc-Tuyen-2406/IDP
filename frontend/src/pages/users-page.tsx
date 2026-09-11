import { LockKeyhole, Plus, ShieldCheck, Trash2, UserPlus } from "lucide-react";
import { useEffect, useState } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { hasPermission } from "@/lib/access";
import {
  apiRequest,
  type DepartmentItem,
  type PageResponse,
  type RoleItem,
  type UserItem,
} from "@/lib/api";
import { useAuth } from "@/state/auth-store";

type UserForm = {
  fullName: string;
  email: string;
  password: string;
  phone: string;
  departmentId: string;
  roleIds: number[];
};

const initialForm: UserForm = {
  fullName: "",
  email: "",
  password: "",
  phone: "",
  departmentId: "",
  roleIds: [],
};

export function UsersPage() {
  const { profile, token } = useAuth();
  const [users, setUsers] = useState<UserItem[]>([]);
  const [roles, setRoles] = useState<RoleItem[]>([]);
  const [departments, setDepartments] = useState<DepartmentItem[]>([]);
  const [form, setForm] = useState<UserForm>(initialForm);
  const [selectedUser, setSelectedUser] = useState<UserItem | null>(null);
  const [selectedRoleIds, setSelectedRoleIds] = useState<number[]>([]);
  const [showCreate, setShowCreate] = useState(false);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const canView = hasPermission(profile, "USER_VIEW");
  const canManage = hasPermission(profile, "USER_MANAGE");

  async function loadData() {
    if (!token || !canView) {
      setUsers([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      const [userPage, roleList, departmentList] = await Promise.all([
        apiRequest<PageResponse<UserItem>>("/api/v1/users?size=100", {}, token),
        canManage ? apiRequest<RoleItem[]>("/api/v1/roles", {}, token) : Promise.resolve([]),
        apiRequest<DepartmentItem[]>("/api/v1/departments", {}, token),
      ]);
      setUsers(userPage.content);
      setRoles(roleList);
      setDepartments(departmentList);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Khong the tai nguoi dung.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData().catch(() => undefined);
  }, [canManage, canView, token]);

  function updateForm<Key extends keyof UserForm>(key: Key, value: UserForm[Key]) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  function toggleRole(roleId: number) {
    setForm((current) => ({
      ...current,
      roleIds: current.roleIds.includes(roleId)
        ? current.roleIds.filter((item) => item !== roleId)
        : [...current.roleIds, roleId],
    }));
  }

  async function createUser(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!token || !canManage) {
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await apiRequest<UserItem>(
        "/api/v1/users",
        {
          method: "POST",
          body: JSON.stringify({
            departmentId: form.departmentId ? Number(form.departmentId) : null,
            fullName: form.fullName.trim(),
            email: form.email.trim(),
            password: form.password,
            phone: form.phone.trim() || null,
            status: "ACTIVE",
            emailVerified: true,
            roleIds: form.roleIds,
          }),
        },
        token,
      );
      setForm(initialForm);
      setShowCreate(false);
      await loadData();
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Khong the tao nguoi dung.");
    } finally {
      setSubmitting(false);
    }
  }

  async function toggleStatus(user: UserItem) {
    if (!token || !canManage) {
      return;
    }
    const nextStatus = user.status === "ACTIVE" ? "INACTIVE" : "ACTIVE";
    try {
      const updated = await apiRequest<UserItem>(
        `/api/v1/users/${user.userId}/status`,
        { method: "PUT", body: JSON.stringify({ status: nextStatus }) },
        token,
      );
      setUsers((current) => current.map((item) => item.userId === updated.userId ? updated : item));
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Khong the cap nhat trang thai nguoi dung.");
    }
  }

  function openRoleAssignment(user: UserItem) {
    setSelectedUser(user);
    setSelectedRoleIds(roles.filter((role) => user.roles.includes(role.roleName)).map((role) => role.roleId));
  }

  async function saveRoleAssignment() {
    if (!token || !canManage || !selectedUser || selectedRoleIds.length === 0) {
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      const existingRoleIds = roles.filter((role) => selectedUser.roles.includes(role.roleName)).map((role) => role.roleId);
      const addedRoleIds = selectedRoleIds.filter((roleId) => !existingRoleIds.includes(roleId));
      const removedRoleIds = existingRoleIds.filter((roleId) => !selectedRoleIds.includes(roleId));
      if (addedRoleIds.length > 0) {
        await apiRequest<UserItem>(
          `/api/v1/users/${selectedUser.userId}/roles`,
          { method: "POST", body: JSON.stringify({ roleIds: addedRoleIds }) },
          token,
        );
      }
      await Promise.all(removedRoleIds.map((roleId) => apiRequest<void>(
        `/api/v1/users/${selectedUser.userId}/roles/${roleId}`,
        { method: "DELETE" },
        token,
      )));
      const updated = await apiRequest<UserItem>(`/api/v1/users/${selectedUser.userId}`, {}, token);
      setUsers((current) => current.map((user) => user.userId === updated.userId ? updated : user));
      setSelectedUser(updated);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Khong the cap nhat role nguoi dung.");
    } finally {
      setSubmitting(false);
    }
  }

  async function deleteUser(user: UserItem) {
    if (!token || !canManage || !window.confirm(`Delete ${user.fullName}? This disables the account record.`)) {
      return;
    }
    try {
      await apiRequest<void>(`/api/v1/users/${user.userId}`, { method: "DELETE" }, token);
      setUsers((current) => current.filter((item) => item.userId !== user.userId));
      if (selectedUser?.userId === user.userId) {
        setSelectedUser(null);
      }
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Khong the xoa nguoi dung.");
    }
  }

  if (!canView) {
    return (
      <Card className="max-w-2xl">
        <LockKeyhole className="h-7 w-7 text-brand-600" />
        <h2 className="mt-4 text-2xl font-semibold text-slate-900">User administration is restricted</h2>
        <p className="mt-3 text-sm leading-7 text-slate-600">Only administrators and managers can access user records. Your permissions are still enforced by the backend.</p>
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      <Card className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <p className="text-sm uppercase tracking-[0.24em] text-brand-600">Access administration</p>
          <h2 className="mt-2 text-2xl font-semibold text-slate-900">Users, departments, and role-based access</h2>
        </div>
        {canManage && (
          <Button onClick={() => setShowCreate((value) => !value)}>
            <UserPlus className="h-4 w-4" />
            {showCreate ? "Close form" : "Create user"}
          </Button>
        )}
      </Card>

      {error && <Card className="border-rose-200 bg-rose-50 text-rose-700">{error}</Card>}

      {showCreate && canManage && (
        <Card>
          <div className="flex items-center gap-3">
            <Plus className="h-5 w-5 text-brand-600" />
            <h3 className="text-lg font-semibold text-slate-900">Create a real account</h3>
          </div>
          <form className="mt-5 grid gap-4 md:grid-cols-2" onSubmit={createUser}>
            <FormField label="Full name"><Input required value={form.fullName} onChange={(event) => updateForm("fullName", event.target.value)} /></FormField>
            <FormField label="Email"><Input required type="email" value={form.email} onChange={(event) => updateForm("email", event.target.value)} /></FormField>
            <FormField label="Temporary password"><Input required minLength={8} type="password" value={form.password} onChange={(event) => updateForm("password", event.target.value)} /></FormField>
            <FormField label="Phone"><Input value={form.phone} onChange={(event) => updateForm("phone", event.target.value)} /></FormField>
            <FormField label="Department">
              <select className="h-11 w-full rounded-2xl border border-slate-200 bg-white px-4 text-sm outline-none focus:border-brand-400 focus:ring-4 focus:ring-brand-100" value={form.departmentId} onChange={(event) => updateForm("departmentId", event.target.value)}>
                <option value="">No department</option>
                {departments.map((department) => <option key={department.departmentId} value={department.departmentId}>{department.departmentName}</option>)}
              </select>
            </FormField>
            <div className="md:col-span-2">
              <p className="text-sm font-medium text-slate-700">Roles *</p>
              <div className="mt-3 flex flex-wrap gap-3">
                {roles.map((role) => (
                  <label key={role.roleId} className="flex cursor-pointer items-center gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700">
                    <input checked={form.roleIds.includes(role.roleId)} type="checkbox" onChange={() => toggleRole(role.roleId)} />
                    {role.roleName}
                  </label>
                ))}
              </div>
            </div>
            <div className="md:col-span-2 flex justify-end">
              <Button disabled={submitting || form.roleIds.length === 0} type="submit">
                <ShieldCheck className="h-4 w-4" />
                {submitting ? "Creating..." : "Create user"}
              </Button>
            </div>
          </form>
        </Card>
      )}

      {selectedUser && canManage && (
        <Card>
          <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
            <div><p className="text-sm uppercase tracking-[0.24em] text-brand-600">Role assignment</p><h3 className="mt-2 text-xl font-semibold text-slate-900">{selectedUser.fullName}</h3><p className="mt-1 text-sm text-slate-500">Select at least one role for this account.</p></div>
            <Button variant="outline" onClick={() => setSelectedUser(null)}>Close</Button>
          </div>
          <div className="mt-5 flex flex-wrap gap-3">
            {roles.map((role) => <label key={role.roleId} className="flex cursor-pointer items-center gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700"><input checked={selectedRoleIds.includes(role.roleId)} type="checkbox" onChange={() => setSelectedRoleIds((current) => current.includes(role.roleId) ? current.filter((id) => id !== role.roleId) : [...current, role.roleId])} />{role.roleName}</label>)}
          </div>
          <Button className="mt-5" disabled={submitting || selectedRoleIds.length === 0} onClick={saveRoleAssignment}><ShieldCheck className="h-4 w-4" />{submitting ? "Saving..." : "Save roles"}</Button>
        </Card>
      )}

      <Card>
        <div className="overflow-x-auto">
          <table className="min-w-full text-left text-sm">
            <thead className="text-slate-500">
              <tr>
                <th className="pb-3">User</th>
                <th className="pb-3">Department</th>
                <th className="pb-3">Roles</th>
                <th className="pb-3">Status</th>
                <th className="pb-3">Action</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.userId} className="border-t border-slate-100">
                  <td className="py-4"><p className="font-semibold text-slate-900">{user.fullName}</p><p className="text-xs text-slate-500">{user.email}</p></td>
                  <td className="py-4">{user.departmentName || "Unassigned"}</td>
                  <td className="py-4">{user.roles.join(", ")}</td>
                  <td className="py-4"><Badge tone={user.status === "ACTIVE" ? "success" : user.status === "LOCKED" ? "danger" : "warning"}>{user.status}</Badge></td>
                  <td className="py-4">
                    <div className="flex flex-wrap gap-2">
                      {canManage && <Button size="sm" variant="outline" onClick={() => toggleStatus(user)}>{user.status === "ACTIVE" ? "Deactivate" : "Activate"}</Button>}
                      {canManage && <Button size="sm" variant="outline" onClick={() => openRoleAssignment(user)}>Roles</Button>}
                      {canManage && <Button size="sm" variant="ghost" className="text-rose-600 hover:bg-rose-50 hover:text-rose-700" onClick={() => deleteUser(user)}><Trash2 className="h-4 w-4" />Delete</Button>}
                      {!canManage && <span className="text-xs font-medium text-slate-500">View only</span>}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {!loading && users.length === 0 && <p className="py-8 text-center text-sm text-slate-500">No user records found.</p>}
        </div>
      </Card>
    </div>
  );
}

function FormField({ children, label }: { children: React.ReactNode; label: string }) {
  return (
    <label>
      <span className="mb-2 block text-sm font-medium text-slate-700">{label}</span>
      {children}
    </label>
  );
}
