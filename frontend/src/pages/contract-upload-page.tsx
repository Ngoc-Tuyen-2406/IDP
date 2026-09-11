import { FileUp, LoaderCircle, Sparkles } from "lucide-react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  apiRequest,
  type ContractDetail,
  type DocumentTypeItem,
  type PageResponse,
  type PartnerItem,
} from "@/lib/api";
import { useAuth } from "@/state/auth-store";

type UploadForm = {
  documentTypeId: string;
  partnerId: string;
  contractNumber: string;
  contractName: string;
  signedDate: string;
  effectiveDate: string;
  expiredDate: string;
  totalValue: string;
  currency: string;
  changeNote: string;
};

const initialForm: UploadForm = {
  documentTypeId: "",
  partnerId: "",
  contractNumber: "",
  contractName: "",
  signedDate: "",
  effectiveDate: "",
  expiredDate: "",
  totalValue: "",
  currency: "VND",
  changeNote: "Initial uploaded version",
};

export function ContractUploadPage() {
  const navigate = useNavigate();
  const { token } = useAuth();
  const [form, setForm] = useState<UploadForm>(initialForm);
  const [file, setFile] = useState<File | null>(null);
  const [partners, setPartners] = useState<PartnerItem[]>([]);
  const [documentTypes, setDocumentTypes] = useState<DocumentTypeItem[]>([]);
  const [loadingOptions, setLoadingOptions] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) {
      return;
    }
    Promise.all([
      apiRequest<PageResponse<PartnerItem>>("/api/v1/partners?size=100", {}, token),
      apiRequest<DocumentTypeItem[]>("/api/v1/document-types", {}, token),
    ])
      .then(([partnerPage, types]) => {
        setPartners(partnerPage.content);
        setDocumentTypes(types);
      })
      .catch((reason: unknown) => {
        setError(reason instanceof Error ? reason.message : "Khong the tai du lieu danh muc.");
      })
      .finally(() => setLoadingOptions(false));
  }, [token]);

  function updateField<Key extends keyof UploadForm>(key: Key, value: UploadForm[Key]) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!token || !file) {
      setError("Hay chon file hop dong PDF hoac anh scan truoc khi upload.");
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      const body = new FormData();
      body.set("file", file);
      body.set("documentTypeId", form.documentTypeId);
      body.set("partnerId", form.partnerId);
      body.set("contractNumber", form.contractNumber.trim());
      body.set("contractName", form.contractName.trim());
      body.set("currency", form.currency.trim().toUpperCase());
      body.set("status", "PROCESSING");
      for (const key of ["signedDate", "effectiveDate", "expiredDate", "totalValue", "changeNote"] as const) {
        if (form[key].trim()) {
          body.set(key, form[key].trim());
        }
      }

      const contract = await apiRequest<ContractDetail>(
        "/api/v1/contracts/upload",
        { method: "POST", body },
        token,
      );
      navigate(`/app/contracts/${contract.contractId}`);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Upload hop dong that bai.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="mx-auto max-w-6xl space-y-4">
      <Card className="relative overflow-hidden border-brand-500/20 bg-gradient-to-br from-brand-900 via-brand-700 to-cyan-600 text-white">
        <div className="pointer-events-none absolute -right-14 -top-24 h-64 w-64 rounded-full bg-cyan-300/20 blur-2xl" />
        <div className="grid gap-6 lg:grid-cols-[1fr_auto] lg:items-end">
          <div className="relative">
            <p className="text-sm font-semibold uppercase tracking-[0.24em] text-cyan-100">New intake</p>
            <h2 className="mt-3 max-w-2xl text-3xl font-semibold text-white">Upload a contract for an accountable AI review</h2>
            <p className="mt-3 max-w-2xl text-sm leading-7 text-white/90">
              Your original file remains attached to the contract version. The review screen can then start OCR, signature detection,
              metadata extraction, risk analysis, and retrieval indexing.
            </p>
          </div>
          <div className="relative rounded-[28px] border border-white/25 bg-white/15 p-5 backdrop-blur">
            <Sparkles className="h-9 w-9 text-cyan-100" />
            <p className="mt-3 text-sm font-semibold text-white">Local first workflow</p>
            <p className="mt-1 text-xs leading-5 text-white/85">PDF, JPG, PNG, TIFF, or WEBP up to the backend file limit.</p>
          </div>
        </div>
      </Card>

      {error && <Card className="border-rose-200 bg-rose-50 text-rose-700">{error}</Card>}

      <form className="grid gap-4 xl:grid-cols-[1.1fr_0.9fr]" onSubmit={submit}>
        <Card>
          <div className="flex items-center gap-3">
            <FileUp className="h-5 w-5 text-brand-600" />
            <div>
              <h3 className="text-lg font-semibold text-slate-900">Contract information</h3>
              <p className="mt-1 text-sm text-slate-500">Fields marked by the server as required must be provided.</p>
            </div>
          </div>

          <div className="mt-6 grid gap-4 md:grid-cols-2">
            <Field label="Contract number" required>
              <Input required value={form.contractNumber} onChange={(event) => updateField("contractNumber", event.target.value)} />
            </Field>
            <Field label="Contract name">
              <Input value={form.contractName} onChange={(event) => updateField("contractName", event.target.value)} />
            </Field>
            <Field label="Document type" required>
              <Select required disabled={loadingOptions} value={form.documentTypeId} onChange={(value) => updateField("documentTypeId", value)}>
                <option value="">Select a document type</option>
                {documentTypes.map((item) => <option key={item.documentTypeId} value={item.documentTypeId}>{item.name}</option>)}
              </Select>
            </Field>
            <Field label="Partner" required>
              <Select required disabled={loadingOptions} value={form.partnerId} onChange={(value) => updateField("partnerId", value)}>
                <option value="">Select a partner</option>
                {partners.map((item) => <option key={item.partnerId} value={item.partnerId}>{item.companyName}</option>)}
              </Select>
            </Field>
            <Field label="Signed date"><Input type="date" value={form.signedDate} onChange={(event) => updateField("signedDate", event.target.value)} /></Field>
            <Field label="Effective date"><Input type="date" value={form.effectiveDate} onChange={(event) => updateField("effectiveDate", event.target.value)} /></Field>
            <Field label="Expiry date"><Input type="date" value={form.expiredDate} onChange={(event) => updateField("expiredDate", event.target.value)} /></Field>
            <Field label="Contract value"><Input type="number" min="0" step="0.01" value={form.totalValue} onChange={(event) => updateField("totalValue", event.target.value)} /></Field>
            <Field label="Currency"><Input maxLength={10} value={form.currency} onChange={(event) => updateField("currency", event.target.value)} /></Field>
          </div>

          <Field className="mt-4" label="Version note">
            <Textarea className="min-h-[100px]" value={form.changeNote} onChange={(event) => updateField("changeNote", event.target.value)} />
          </Field>
        </Card>

        <Card className="flex flex-col">
          <p className="text-sm uppercase tracking-[0.24em] text-brand-600">Source document</p>
          <h3 className="mt-2 text-2xl font-semibold text-slate-900">Attach the original contract</h3>
          <label className="mt-6 flex min-h-64 cursor-pointer flex-col items-center justify-center rounded-[28px] border-2 border-dashed border-brand-200 bg-brand-50/60 p-8 text-center transition hover:border-brand-500 hover:bg-brand-50">
            <FileUp className="h-10 w-10 text-brand-600" />
            <span className="mt-4 text-sm font-semibold text-slate-900">{file ? file.name : "Choose a document"}</span>
            <span className="mt-2 text-xs leading-5 text-slate-500">The file is saved with this contract version and never sent directly from React to AI.</span>
            <input className="sr-only" type="file" accept=".pdf,.jpg,.jpeg,.png,.tif,.tiff,.webp" onChange={(event) => setFile(event.target.files?.[0] ?? null)} />
          </label>
          {file && <p className="mt-4 text-sm text-slate-600">{(file.size / 1024 / 1024).toFixed(2)} MB</p>}
          <div className="mt-auto pt-6">
            <Button className="w-full" type="submit" disabled={submitting || loadingOptions}>
              {submitting ? <LoaderCircle className="h-4 w-4 animate-spin" /> : <FileUp className="h-4 w-4" />}
              {submitting ? "Uploading..." : "Create contract and upload"}
            </Button>
          </div>
        </Card>
      </form>
    </div>
  );
}

function Field({ children, className, label, required }: { children: React.ReactNode; className?: string; label: string; required?: boolean }) {
  return (
    <label className={className}>
      <span className="mb-2 block text-sm font-medium text-slate-700">{label}{required ? " *" : ""}</span>
      {children}
    </label>
  );
}

function Select({ children, disabled, onChange, required, value }: { children: React.ReactNode; disabled?: boolean; onChange: (value: string) => void; required?: boolean; value: string }) {
  return (
    <select
      className="flex h-11 w-full rounded-2xl border border-slate-200 bg-white/85 px-4 text-sm text-slate-900 outline-none transition focus:border-brand-400 focus:ring-4 focus:ring-brand-100 disabled:cursor-not-allowed disabled:opacity-60"
      disabled={disabled}
      required={required}
      value={value}
      onChange={(event) => onChange(event.target.value)}
    >
      {children}
    </select>
  );
}
