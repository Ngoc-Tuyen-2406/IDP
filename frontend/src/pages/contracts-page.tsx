import { FileUp, Heart, Search, SlidersHorizontal } from "lucide-react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { hasPermission } from "@/lib/access";
import { apiRequest, type ContractSummary, type DocumentTypeItem, type PageResponse, type PartnerItem } from "@/lib/api";
import { formatCurrency, formatDate } from "@/lib/format";
import { useAuth } from "@/state/auth-store";

export function ContractsPage() {
  const navigate = useNavigate();
  const { profile, token } = useAuth();
  const [keyword, setKeyword] = useState("");
  const [partnerId, setPartnerId] = useState("");
  const [documentTypeId, setDocumentTypeId] = useState("");
  const [status, setStatus] = useState("");
  const [favoritesOnly, setFavoritesOnly] = useState(false);
  const [contracts, setContracts] = useState<PageResponse<ContractSummary> | null>(null);
  const [partners, setPartners] = useState<PartnerItem[]>([]);
  const [documentTypes, setDocumentTypes] = useState<DocumentTypeItem[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) {
      return;
    }
    const params = new URLSearchParams({ size: "100" });
    if (keyword.trim()) params.set("keyword", keyword.trim());
    if (partnerId) params.set("partnerId", partnerId);
    if (documentTypeId) params.set("documentTypeId", documentTypeId);
    if (status) params.set("status", status);
    const path = favoritesOnly ? "/api/v1/contracts/favorites?size=100" : `/api/v1/contracts?${params.toString()}`;
    apiRequest<PageResponse<ContractSummary>>(path, {}, token)
      .then(setContracts)
      .catch((err) => setError(err instanceof Error ? err.message : "Khong the tai danh sach hop dong"));
  }, [documentTypeId, favoritesOnly, keyword, partnerId, status, token]);

  useEffect(() => {
    if (!token) {
      return;
    }
    Promise.allSettled([
      apiRequest<PageResponse<PartnerItem>>("/api/v1/partners?size=100", {}, token),
      apiRequest<DocumentTypeItem[]>("/api/v1/document-types", {}, token),
    ]).then(([partnerResult, typeResult]) => {
      setPartners(partnerResult.status === "fulfilled" ? partnerResult.value.content : []);
      setDocumentTypes(typeResult.status === "fulfilled" ? typeResult.value : []);
    });
  }, [token]);

  async function toggleFavorite(event: React.MouseEvent<HTMLButtonElement>, contract: ContractSummary) {
    event.stopPropagation();
    if (!token) {
      return;
    }
    try {
      await apiRequest<void>(`/api/v1/contracts/${contract.contractId}/favorite`, { method: contract.favorite ? "DELETE" : "POST", body: JSON.stringify({}) }, token);
      setContracts((current) => current == null ? current : {
        ...current,
        content: favoritesOnly && contract.favorite
          ? current.content.filter((item) => item.contractId !== contract.contractId)
          : current.content.map((item) => item.contractId === contract.contractId ? { ...item, favorite: !item.favorite } : item),
      });
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Khong the cap nhat yeu thich.");
    }
  }

  return (
    <div className="space-y-4">
      <Card className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <p className="text-sm uppercase tracking-[0.24em] text-brand-600">Contracts</p>
          <h2 className="mt-2 text-2xl font-semibold text-slate-900">Search and monitor contract records</h2>
        </div>
        <div className="relative w-full md:max-w-md">
          <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <Input className="pl-11" placeholder="Search by name or number" value={keyword} onChange={(event) => setKeyword(event.target.value)} />
        </div>
        {hasPermission(profile, "CONTRACT_CREATE") && (
          <button
            className="inline-flex h-11 items-center justify-center gap-2 rounded-2xl bg-brand-600 px-5 text-sm font-semibold text-white shadow-card transition hover:bg-brand-700"
            onClick={() => navigate("/app/contracts/upload")}
          >
            <FileUp className="h-4 w-4" />
            Upload contract
          </button>
        )}
      </Card>

      {error && <Card className="border-rose-200 bg-rose-50 text-rose-700">{error}</Card>}

      <Card className="flex flex-col gap-4 xl:flex-row xl:items-center">
        <div className="flex items-center gap-3 text-slate-700"><SlidersHorizontal className="h-5 w-5 text-brand-600" /><span className="text-sm font-semibold">Filters</span></div>
        <select className="h-11 rounded-2xl border border-slate-200 bg-white px-4 text-sm text-slate-800 outline-none focus:border-brand-400 focus:ring-4 focus:ring-brand-100" value={partnerId} onChange={(event) => setPartnerId(event.target.value)}><option value="">All partners</option>{partners.map((partner) => <option key={partner.partnerId} value={partner.partnerId}>{partner.companyName}</option>)}</select>
        <select className="h-11 rounded-2xl border border-slate-200 bg-white px-4 text-sm text-slate-800 outline-none focus:border-brand-400 focus:ring-4 focus:ring-brand-100" value={documentTypeId} onChange={(event) => setDocumentTypeId(event.target.value)}><option value="">All document types</option>{documentTypes.map((type) => <option key={type.documentTypeId} value={type.documentTypeId}>{type.name}</option>)}</select>
        <select className="h-11 rounded-2xl border border-slate-200 bg-white px-4 text-sm text-slate-800 outline-none focus:border-brand-400 focus:ring-4 focus:ring-brand-100" value={status} onChange={(event) => setStatus(event.target.value)}><option value="">All statuses</option><option value="DRAFT">Draft</option><option value="PROCESSING">Processing</option><option value="PENDING">Pending</option><option value="APPROVED">Approved</option><option value="REJECTED">Rejected</option><option value="EXPIRED">Expired</option><option value="TERMINATED">Terminated</option></select>
        <Button className="xl:ml-auto" size="sm" variant={favoritesOnly ? "default" : "outline"} onClick={() => setFavoritesOnly((current) => !current)}><Heart className={favoritesOnly ? "h-4 w-4 fill-current" : "h-4 w-4"} />Favorites</Button>
      </Card>

      <div className="grid gap-4 xl:grid-cols-[1.15fr_0.85fr]">
        <Card>
          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="text-slate-500">
                <tr>
                  <th className="pb-3" />
                  <th className="pb-3">Contract</th>
                  <th className="pb-3">Type</th>
                  <th className="pb-3">Partner</th>
                  <th className="pb-3">Status</th>
                </tr>
              </thead>
              <tbody>
                {contracts?.content.map((contract) => (
                  <tr
                    key={contract.contractId}
                    className="cursor-pointer border-t border-slate-100 transition hover:bg-brand-50/60"
                    onClick={() => navigate(`/app/contracts/${contract.contractId}`)}
                  >
                    <td className="py-4"><Button size="icon" variant="ghost" aria-label={contract.favorite ? "Remove favorite" : "Add favorite"} onClick={(event) => toggleFavorite(event, contract)}><Heart className={contract.favorite ? "h-4 w-4 fill-rose-500 text-rose-500" : "h-4 w-4 text-slate-400"} /></Button></td>
                    <td className="py-4">
                      <p className="font-semibold text-slate-900">{contract.contractName || contract.contractNumber}</p>
                      <p className="text-xs text-slate-500">{contract.contractNumber}</p>
                    </td>
                    <td className="py-4">{contract.documentTypeName}</td>
                    <td className="py-4">{contract.partnerName}</td>
                    <td className="py-4">
                      <Badge tone={contract.status === "Approved" ? "success" : contract.status === "Rejected" ? "danger" : "info"}>
                        {contract.status}
                      </Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {contracts?.content.length === 0 && <p className="py-8 text-center text-sm text-slate-500">No contracts match the current filters.</p>}
          </div>
        </Card>

        <Card className="bg-gradient-to-br from-white to-brand-50">
          <p className="text-sm uppercase tracking-[0.24em] text-brand-600">Quick insight</p>
          <div className="mt-5 space-y-4">
            {contracts?.content.slice(0, 4).map((contract) => (
              <div key={contract.contractId} className="rounded-[24px] border border-brand-100 bg-white/85 p-4">
                <div className="flex items-center justify-between gap-3">
                  <p className="font-semibold text-slate-900">{contract.contractNumber}</p>
                  <Badge tone="info">{contract.status}</Badge>
                </div>
                <p className="mt-2 text-sm text-slate-600">{contract.partnerName}</p>
                <div className="mt-4 flex items-center justify-between text-sm text-slate-500">
                  <span>{formatDate(contract.expiredDate)}</span>
                  <span>{formatCurrency(contract.totalValue, contract.currency)}</span>
                </div>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
}
