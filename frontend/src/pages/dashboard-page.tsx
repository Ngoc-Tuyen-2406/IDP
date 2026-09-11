import { useEffect, useState } from "react";

import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { apiRequest, type ContractSummary, type DashboardStatistics, type PageResponse, type StatusMetric } from "@/lib/api";
import { formatDate } from "@/lib/format";
import { useAuth } from "@/state/auth-store";

const statusColors = ["#2563eb", "#06b6d4", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6", "#64748b"];

export function DashboardPage() {
  const { token } = useAuth();
  const [statistics, setStatistics] = useState<DashboardStatistics | null>(null);
  const [metrics, setMetrics] = useState<StatusMetric[]>([]);
  const [recentContracts, setRecentContracts] = useState<PageResponse<ContractSummary> | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) {
      return;
    }

    Promise.all([
      apiRequest<DashboardStatistics>("/api/v1/dashboard/statistics", {}, token),
      apiRequest<StatusMetric[]>("/api/v1/dashboard/contracts-by-status", {}, token),
      apiRequest<PageResponse<ContractSummary>>("/api/v1/dashboard/recent?limit=5", {}, token),
    ])
      .then(([stats, statusMetrics, recent]) => {
        setStatistics(stats);
        setMetrics(statusMetrics);
        setRecentContracts(recent);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Khong the tai dashboard"));
  }, [token]);

  const statCards = [
    { label: "Total contracts", value: statistics?.totalContracts ?? 0 },
    { label: "Processing", value: statistics?.processingContracts ?? 0 },
    { label: "Pending review", value: statistics?.pendingContracts ?? 0 },
    { label: "Approved", value: statistics?.approvedContracts ?? 0 },
    { label: "Rejected", value: statistics?.rejectedContracts ?? 0 },
    { label: "Expiring this month", value: statistics?.expiringContracts ?? 0 },
  ];

  const totalByStatus = metrics.reduce((total, metric) => total + metric.count, 0);
  let currentPercentage = 0;
  const chartSegments = metrics.map((metric, index) => {
    const start = currentPercentage;
    currentPercentage += totalByStatus > 0 ? (metric.count / totalByStatus) * 100 : 0;
    return `${statusColors[index % statusColors.length]} ${start}% ${currentPercentage}%`;
  });
  const chartBackground = totalByStatus > 0 ? `conic-gradient(${chartSegments.join(", ")})` : "#e2e8f0";

  return (
    <div className="space-y-4">
      {error && <Card className="border-rose-200 bg-rose-50 text-rose-700">{error}</Card>}

      <div className="grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
        <Card className="relative overflow-hidden border-brand-500/20 bg-gradient-to-br from-brand-800 via-brand-600 to-cyan-600 text-white">
          <div className="pointer-events-none absolute -right-20 -top-24 h-64 w-64 rounded-full bg-cyan-300/20 blur-2xl" />
          <div className="pointer-events-none absolute -bottom-28 left-1/3 h-56 w-56 rounded-full bg-white/10 blur-2xl" />
          <p className="relative text-sm font-semibold uppercase tracking-[0.24em] text-cyan-100">Executive overview</p>
          <h2 className="relative mt-3 text-3xl font-semibold text-white">A calm command center for contract operations</h2>
          <p className="relative mt-3 max-w-2xl text-sm leading-7 text-white/90">
            Track contract throughput, review pending approvals, and monitor expiring agreements without leaving the main workspace.
          </p>
          <div className="relative mt-8 grid gap-4 sm:grid-cols-3">
            {statCards.slice(0, 3).map((card) => (
              <div key={card.label} className="rounded-[24px] border border-white/25 bg-white/15 p-4 shadow-inner backdrop-blur-sm">
                <p className="text-sm font-medium text-white/85">{card.label}</p>
                <p className="mt-4 text-3xl font-bold text-white">{card.value}</p>
              </div>
            ))}
          </div>
        </Card>

        <Card className="border-brand-100 bg-gradient-to-br from-white to-brand-50/70">
          <p className="text-sm font-semibold uppercase tracking-[0.24em] text-brand-700">Status mix</p>
          <div className="mt-6 grid items-center gap-6 sm:grid-cols-[190px_1fr] xl:grid-cols-1 2xl:grid-cols-[190px_1fr]">
            <div
              className="relative mx-auto flex h-44 w-44 items-center justify-center rounded-full shadow-[0_16px_35px_rgba(30,99,209,0.18)]"
              style={{ background: chartBackground }}
              role="img"
              aria-label={`Contract status chart. ${totalByStatus} contracts in total.`}
            >
              <div className="flex h-28 w-28 flex-col items-center justify-center rounded-full border border-white bg-white shadow-inner">
                <span className="text-3xl font-bold text-slate-900">{totalByStatus}</span>
                <span className="mt-1 text-xs font-semibold uppercase tracking-[0.16em] text-slate-500">Contracts</span>
              </div>
            </div>
            <div className="grid gap-2">
              {metrics.map((metric, index) => (
                <div key={metric.status} className="flex items-center justify-between rounded-2xl border border-white bg-white/75 px-3 py-2 shadow-sm">
                  <div className="flex min-w-0 items-center gap-2">
                    <span className="h-3 w-3 shrink-0 rounded-full" style={{ backgroundColor: statusColors[index % statusColors.length] }} />
                    <span className="truncate text-sm font-medium capitalize text-slate-700">{metric.status.toLowerCase()}</span>
                  </div>
                  <span className="ml-3 text-sm font-bold text-slate-900">{metric.count}</span>
                </div>
              ))}
              {metrics.length === 0 && <p className="text-center text-sm text-slate-500">No status data available.</p>}
            </div>
          </div>
        </Card>
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        {statCards.slice(3).map((card, index) => (
          <Card key={card.label} className={index === 0 ? "border-emerald-100 bg-emerald-50/70" : index === 1 ? "border-rose-100 bg-rose-50/70" : "border-amber-100 bg-amber-50/70"}>
            <p className="text-sm font-semibold text-slate-700">{card.label}</p>
            <p className={index === 0 ? "mt-4 text-4xl font-bold text-emerald-700" : index === 1 ? "mt-4 text-4xl font-bold text-rose-700" : "mt-4 text-4xl font-bold text-amber-700"}>{card.value}</p>
          </Card>
        ))}
      </div>

      <Card>
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm uppercase tracking-[0.24em] text-brand-600">Recent</p>
            <h3 className="mt-2 text-2xl font-semibold text-slate-900">Latest contracts</h3>
          </div>
        </div>
        <div className="mt-6 overflow-x-auto">
          <table className="min-w-full text-left text-sm">
            <thead className="text-slate-500">
              <tr>
                <th className="pb-3">Contract</th>
                <th className="pb-3">Partner</th>
                <th className="pb-3">Status</th>
                <th className="pb-3">Effective</th>
                <th className="pb-3">Updated</th>
              </tr>
            </thead>
            <tbody>
              {recentContracts?.content.map((contract) => (
                <tr key={contract.contractId} className="border-t border-slate-100">
                  <td className="py-4">
                    <p className="font-semibold text-slate-900">{contract.contractName || contract.contractNumber}</p>
                    <p className="text-xs text-slate-500">{contract.contractNumber}</p>
                  </td>
                  <td className="py-4">{contract.partnerName}</td>
                  <td className="py-4">
                    <Badge tone={contract.status === "Approved" ? "success" : contract.status === "Rejected" ? "danger" : "info"}>
                      {contract.status}
                    </Badge>
                  </td>
                  <td className="py-4">{formatDate(contract.effectiveDate)}</td>
                  <td className="py-4">{formatDate(contract.updatedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}
