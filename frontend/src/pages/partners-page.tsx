import { Building2, Pencil, Plus, Trash2 } from "lucide-react";
import { useEffect, useState } from "react";

import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { hasPermission } from "@/lib/access";
import { apiRequest, type PageResponse, type PartnerItem } from "@/lib/api";
import { useAuth } from "@/state/auth-store";

type PartnerForm = {
  companyName: string;
  partnerType: string;
  taxCode: string;
  email: string;
  phone: string;
  address: string;
  website: string;
};

const initialForm: PartnerForm = {
  companyName: "",
  partnerType: "Business",
  taxCode: "",
  email: "",
  phone: "",
  address: "",
  website: "",
};

export function PartnersPage() {
  const { profile, token } = useAuth();
  const [partners, setPartners] = useState<PageResponse<PartnerItem> | null>(null);
  const [form, setForm] = useState<PartnerForm>(initialForm);
  const [editingPartnerId, setEditingPartnerId] = useState<number | null>(null);
  const [showCreate, setShowCreate] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const canManage = hasPermission(profile, "PARTNER_MANAGE");

  async function loadPartners() {
    if (!token) {
      return;
    }
    const data = await apiRequest<PageResponse<PartnerItem>>("/api/v1/partners?size=100", {}, token);
    setPartners(data);
  }

  useEffect(() => {
    loadPartners().catch((reason: unknown) => {
      setError(reason instanceof Error ? reason.message : "Khong the tai doi tac.");
    });
  }, [token]);

  function updateForm<Key extends keyof PartnerForm>(key: Key, value: PartnerForm[Key]) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  async function savePartner(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!token || !canManage) {
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await apiRequest<PartnerItem>(
        editingPartnerId == null ? "/api/v1/partners" : `/api/v1/partners/${editingPartnerId}`,
        {
          method: editingPartnerId == null ? "POST" : "PUT",
          body: JSON.stringify({
            companyName: form.companyName.trim(),
            partnerType: form.partnerType.trim() || null,
            taxCode: form.taxCode.trim() || null,
            email: form.email.trim() || null,
            phone: form.phone.trim() || null,
            address: form.address.trim() || null,
            website: form.website.trim() || null,
          }),
        },
        token,
      );
      setForm(initialForm);
      setEditingPartnerId(null);
      setShowCreate(false);
      await loadPartners();
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Khong the tao doi tac.");
    } finally {
      setSubmitting(false);
    }
  }

  function startEditing(partner: PartnerItem) {
    setEditingPartnerId(partner.partnerId);
    setForm({
      companyName: partner.companyName,
      partnerType: partner.partnerType ?? "",
      taxCode: partner.taxCode ?? "",
      email: partner.email ?? "",
      phone: partner.phone ?? "",
      address: partner.address ?? "",
      website: partner.website ?? "",
    });
    setShowCreate(true);
  }

  async function deletePartner(partnerId: number) {
    if (!token || !canManage || !window.confirm("Delete this partner? Contracts referencing it may prevent deletion.")) {
      return;
    }
    try {
      await apiRequest<void>(`/api/v1/partners/${partnerId}`, { method: "DELETE" }, token);
      await loadPartners();
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Khong the xoa doi tac.");
    }
  }

  return (
    <div className="space-y-4">
      <Card className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <p className="text-sm uppercase tracking-[0.24em] text-brand-600">Partners</p>
          <h2 className="mt-2 text-2xl font-semibold text-slate-900">Company directory and contract counterparties</h2>
        </div>
        {canManage && (
          <Button onClick={() => {
            setShowCreate((value) => !value);
            if (showCreate) {
              setEditingPartnerId(null);
              setForm(initialForm);
            }
          }}>
            <Plus className="h-4 w-4" />
            {showCreate ? "Close form" : "Add partner"}
          </Button>
        )}
      </Card>

      {error && <Card className="border-rose-200 bg-rose-50 text-rose-700">{error}</Card>}

      {showCreate && canManage && (
        <Card>
          <div className="flex items-center gap-3">
            <Building2 className="h-5 w-5 text-brand-600" />
            <h3 className="text-lg font-semibold text-slate-900">{editingPartnerId == null ? "Add a contract partner" : "Edit contract partner"}</h3>
          </div>
          <form className="mt-5 grid gap-4 md:grid-cols-2" onSubmit={savePartner}>
            <FormField label="Company name"><Input required value={form.companyName} onChange={(event) => updateForm("companyName", event.target.value)} /></FormField>
            <FormField label="Partner type"><Input value={form.partnerType} onChange={(event) => updateForm("partnerType", event.target.value)} /></FormField>
            <FormField label="Tax code"><Input value={form.taxCode} onChange={(event) => updateForm("taxCode", event.target.value)} /></FormField>
            <FormField label="Email"><Input type="email" value={form.email} onChange={(event) => updateForm("email", event.target.value)} /></FormField>
            <FormField label="Phone"><Input value={form.phone} onChange={(event) => updateForm("phone", event.target.value)} /></FormField>
            <FormField label="Website"><Input value={form.website} onChange={(event) => updateForm("website", event.target.value)} /></FormField>
            <FormField className="md:col-span-2" label="Address"><Textarea className="min-h-[90px]" value={form.address} onChange={(event) => updateForm("address", event.target.value)} /></FormField>
            <div className="md:col-span-2 flex justify-end gap-3"><Button type="button" variant="outline" onClick={() => { setShowCreate(false); setEditingPartnerId(null); setForm(initialForm); }}>Cancel</Button><Button disabled={submitting} type="submit">{submitting ? "Saving..." : editingPartnerId == null ? "Create partner" : "Save partner"}</Button></div>
          </form>
        </Card>
      )}

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {partners?.content.map((partner) => (
          <Card key={partner.partnerId} className="bg-slate-50/75">
            <p className="text-lg font-semibold text-slate-900">{partner.companyName}</p>
            <p className="mt-2 text-sm text-slate-500">{partner.partnerType || "Partner"}</p>
            <div className="mt-5 space-y-2 text-sm text-slate-600">
              <p>{partner.email || "No email"}</p>
              <p>{partner.phone || "No phone"}</p>
              <p>{partner.taxCode || "No tax code"}</p>
              <p className="line-clamp-2">{partner.address || "No address"}</p>
            </div>
            {canManage && <div className="mt-5 flex gap-2"><Button size="sm" variant="outline" onClick={() => startEditing(partner)}><Pencil className="h-4 w-4" />Edit</Button><Button size="sm" variant="ghost" className="text-rose-600 hover:bg-rose-50 hover:text-rose-700" onClick={() => deletePartner(partner.partnerId)}><Trash2 className="h-4 w-4" />Delete</Button></div>}
          </Card>
        ))}
      </div>
    </div>
  );
}

function FormField({ children, className, label }: { children: React.ReactNode; className?: string; label: string }) {
  return (
    <label className={className}>
      <span className="mb-2 block text-sm font-medium text-slate-700">{label}</span>
      {children}
    </label>
  );
}
