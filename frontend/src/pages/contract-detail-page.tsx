import { Bot, FileText, Download, LoaderCircle, MessageSquare, ScanSearch, ShieldCheck, Sparkles } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { hasPermission } from "@/lib/access";
import {
  apiRequest,
  downloadProtectedFile,
  type CommentItem,
  type ContractClause,
  type ContractDetail,
  type DetectionRegion,
  type MetadataField,
  type OcrResult,
  type ProcessingJob,
  type RiskResponse,
  type SummaryResponse,
  type UserItem,
  type WorkflowStep,
} from "@/lib/api";
import { formatCurrency, formatDate } from "@/lib/format";
import { useAuth } from "@/state/auth-store";

export function ContractDetailPage() {
  const { contractId } = useParams<{ contractId: string }>();
  const { token, profile } = useAuth();
  const [contract, setContract] = useState<ContractDetail | null>(null);
  const [summary, setSummary] = useState<SummaryResponse | null>(null);
  const [risk, setRisk] = useState<RiskResponse | null>(null);
  const [metadata, setMetadata] = useState<MetadataField[]>([]);
  const [clauses, setClauses] = useState<ContractClause[]>([]);
  const [comments, setComments] = useState<CommentItem[]>([]);
  const [workflow, setWorkflow] = useState<WorkflowStep[]>([]);
  const [users, setUsers] = useState<UserItem[]>([]);
  const [ocrResults, setOcrResults] = useState<OcrResult[]>([]);
  const [detectionRegions, setDetectionRegions] = useState<DetectionRegion[]>([]);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [commentText, setCommentText] = useState("");
  const [mentionedUserIds, setMentionedUserIds] = useState<number[]>([]);
  const [replyingTo, setReplyingTo] = useState<number | null>(null);
  const [replyText, setReplyText] = useState("");
  const [editingCommentId, setEditingCommentId] = useState<number | null>(null);
  const [editingCommentText, setEditingCommentText] = useState("");
  const [metadataDraft, setMetadataDraft] = useState<Record<number, string>>({});
  const [processing, setProcessing] = useState(false);
  const [processingJob, setProcessingJob] = useState<ProcessingJob | null>(null);
  const [savingMetadata, setSavingMetadata] = useState(false);
  const [workflowComment, setWorkflowComment] = useState("");
  const [selectedApprovers, setSelectedApprovers] = useState<number[]>([]);
  const [workflowSubmitting, setWorkflowSubmitting] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const canApprove = hasPermission(profile, "CONTRACT_APPROVE");
  const canRunAi = hasPermission(profile, "AI_PROCESS");
  const canUpdateContract = hasPermission(profile, "CONTRACT_UPDATE");
  const activeApprovalStep = workflow.find((step) => step.status.toLowerCase() === "pending");
  const canTakeApprovalAction = canApprove && activeApprovalStep?.approverId === profile?.userId;
  const approvalCandidates = users.filter((user) =>
    user.roles.some((role) => ["admin", "manager"].includes(role.toLowerCase())),
  );

  useEffect(() => {
    if (!token || !contractId) {
      return;
    }

    Promise.allSettled([
      apiRequest<ContractDetail>(`/api/v1/contracts/${contractId}`, {}, token),
      apiRequest<SummaryResponse>(`/api/v1/contracts/${contractId}/summary`, {}, token),
      apiRequest<RiskResponse>(`/api/v1/contracts/${contractId}/risk-report`, {}, token),
      apiRequest<MetadataField[]>(`/api/v1/contracts/${contractId}/metadata`, {}, token),
      apiRequest<ContractClause[]>(`/api/v1/contracts/${contractId}/clauses`, {}, token),
      apiRequest<CommentItem[]>(`/api/v1/contracts/${contractId}/comments`, {}, token),
      apiRequest<WorkflowStep[]>(`/api/v1/contracts/${contractId}/workflow`, {}, token),
    ]).then(([contractResult, summaryResult, riskResult, metadataResult, clausesResult, commentsResult, workflowResult]) => {
      if (contractResult.status === "fulfilled") {
        setContract(contractResult.value);
      } else {
        setError(contractResult.reason instanceof Error ? contractResult.reason.message : "Khong the tai hop dong");
      }
      if (summaryResult.status === "fulfilled") {
        setSummary(summaryResult.value);
      }
      if (riskResult.status === "fulfilled") {
        setRisk(riskResult.value);
      }
      if (metadataResult.status === "fulfilled") {
        setMetadata(metadataResult.value);
      }
      if (clausesResult.status === "fulfilled") {
        setClauses(clausesResult.value);
      }
      if (commentsResult.status === "fulfilled") {
        setComments(commentsResult.value);
      }
      if (workflowResult.status === "fulfilled") {
        setWorkflow(workflowResult.value);
      }
    });
  }, [contractId, refreshKey, token]);

  useEffect(() => {
    if (!token || !canApprove) {
      return;
    }
    apiRequest<{ content: UserItem[] }>("/api/v1/users?size=100", {}, token)
      .then((response) => setUsers(response.content))
      .catch(() => setUsers([]));
  }, [canApprove, token]);

  useEffect(() => {
    setMetadataDraft(Object.fromEntries(metadata.map((item) => [item.metadataId, valueToText(item.currentValue)])));
  }, [metadata]);

  useEffect(() => {
    if (!token || !contract?.currentVersionId) {
      setOcrResults([]);
      setDetectionRegions([]);
      return;
    }
    Promise.allSettled([
      apiRequest<OcrResult[]>(`/api/v1/ocr/${contract.currentVersionId}`, {}, token),
      apiRequest<DetectionRegion[]>(`/api/v1/detection/${contract.currentVersionId}`, {}, token),
    ]).then(([ocr, detection]) => {
      setOcrResults(ocr.status === "fulfilled" ? ocr.value : []);
      setDetectionRegions(detection.status === "fulfilled" ? detection.value : []);
    });
  }, [contract?.currentVersionId, refreshKey, token]);

  useEffect(() => {
    if (!token || !contractId) {
      return;
    }
    let objectUrl: string | null = null;
    downloadProtectedFile(`/api/v1/contracts/${contractId}/download`, token)
      .then((blob) => {
        objectUrl = URL.createObjectURL(blob);
        setPreviewUrl(objectUrl);
      })
      .catch(() => {
        setPreviewUrl(null);
      });
    return () => {
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl);
      }
    };
  }, [contractId, token]);

  const groupedComments = useMemo(() => {
    const roots = comments.filter((comment) => !comment.parentCommentId);
    return roots.map((root) => ({
      ...root,
      replies: comments.filter((comment) => comment.parentCommentId === root.commentId),
    }));
  }, [comments]);

  async function handleCommentSubmit() {
    if (!token || !contractId || !commentText.trim()) {
      return;
    }
    const created = await apiRequest<CommentItem>(
      `/api/v1/contracts/${contractId}/comments`,
      {
        method: "POST",
        body: JSON.stringify({
          content: commentText,
          mentionedUserIds,
        }),
      },
      token,
    );
    setComments((current) => [...current, created]);
    setCommentText("");
    setMentionedUserIds([]);
  }

  async function replyToComment(commentId: number) {
    if (!token || !replyText.trim()) {
      return;
    }
    try {
      const created = await apiRequest<CommentItem>(
        `/api/v1/comments/${commentId}/reply`,
        { method: "POST", body: JSON.stringify({ content: replyText.trim(), mentionedUserIds: [] }) },
        token,
      );
      setComments((current) => [...current, created]);
      setReplyText("");
      setReplyingTo(null);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Khong the tra loi binh luan.");
    }
  }

  async function saveCommentEdit(commentId: number) {
    if (!token || !editingCommentText.trim()) {
      return;
    }
    try {
      const updated = await apiRequest<CommentItem>(
        `/api/v1/comments/${commentId}`,
        { method: "PUT", body: JSON.stringify({ content: editingCommentText.trim(), mentionedUserIds: [] }) },
        token,
      );
      setComments((current) => current.map((comment) => comment.commentId === updated.commentId ? updated : comment));
      setEditingCommentId(null);
      setEditingCommentText("");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Khong the cap nhat binh luan.");
    }
  }

  async function deleteComment(commentId: number) {
    if (!token || !window.confirm("Delete this comment and its replies?")) {
      return;
    }
    try {
      await apiRequest<void>(`/api/v1/comments/${commentId}`, { method: "DELETE" }, token);
      setComments((current) => current.filter((comment) => comment.commentId !== commentId && comment.parentCommentId !== commentId));
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Khong the xoa binh luan.");
    }
  }

  async function runAnalysis() {
    if (!token || !contract?.currentVersionId) {
      return;
    }
    setProcessing(true);
    setError(null);
    try {
      const queuedJob = await apiRequest<ProcessingJob>(
        "/api/v1/processing/jobs",
        {
          method: "POST",
          body: JSON.stringify({
            contractId: contract.contractId,
            versionId: contract.currentVersionId,
            taskType: "FULL_PIPELINE",
          }),
        },
        token,
      );

      setProcessingJob(queuedJob);
      const finishedJob = await waitForProcessingJob(queuedJob, token);
      setProcessingJob(finishedJob);

      if (finishedJob.status.toUpperCase() === "FAILED") {
        setError(finishedJob.errorMessage || "AI Server could not process this document.");
        return;
      }
      setRefreshKey((value) => value + 1);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Khong the chay AI processing.");
    } finally {
      setProcessing(false);
    }
  }

  async function saveReviewedMetadata() {
    if (!token || !contractId || metadata.length === 0) {
      return;
    }
    setSavingMetadata(true);
    setError(null);
    try {
      const updated = await apiRequest<MetadataField[]>(
        `/api/v1/contracts/${contractId}/metadata`,
        {
          method: "PUT",
          body: JSON.stringify({
            fields: metadata.map((item) => ({
              metadataId: item.metadataId,
              fieldName: item.fieldName,
              fieldType: item.fieldType,
              currentValue: metadataDraft[item.metadataId] ?? valueToText(item.currentValue),
              verified: true,
            })),
          }),
        },
        token,
      );
      setMetadata(updated);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Khong the luu metadata da review.");
    } finally {
      setSavingMetadata(false);
    }
  }

  async function submitWorkflow() {
    if (!token || !contractId || selectedApprovers.length === 0) {
      setError("Hay chon it nhat mot nguoi duyet.");
      return;
    }
    setWorkflowSubmitting(true);
    setError(null);
    try {
      const steps = await apiRequest<WorkflowStep[]>(
        `/api/v1/contracts/${contractId}/submit`,
        { method: "POST", body: JSON.stringify({ approverIds: selectedApprovers }) },
        token,
      );
      setWorkflow(steps);
      setSelectedApprovers([]);
      setRefreshKey((value) => value + 1);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "Khong the gui workflow phe duyet.");
    } finally {
      setWorkflowSubmitting(false);
    }
  }

  async function takeWorkflowAction(action: "approve" | "reject") {
    if (!token || !contractId) {
      return;
    }
    setWorkflowSubmitting(true);
    setError(null);
    try {
      const steps = await apiRequest<WorkflowStep[]>(
        `/api/v1/contracts/${contractId}/${action}`,
        { method: "POST", body: JSON.stringify({ comment: workflowComment.trim() || null }) },
        token,
      );
      setWorkflow(steps);
      setWorkflowComment("");
      setRefreshKey((value) => value + 1);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : `Khong the ${action} hop dong.`);
    } finally {
      setWorkflowSubmitting(false);
    }
  }

  return (
    <div className="space-y-4">
      {error && <Card className="border-rose-200 bg-rose-50 text-rose-700">{error}</Card>}

      <Card className="overflow-hidden">
        <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
          <div>
            <p className="text-sm uppercase tracking-[0.24em] text-brand-600">Contract detail</p>
            <h2 className="mt-2 text-3xl font-semibold text-slate-900">
              {contract?.contractName || contract?.contractNumber || "Loading contract"}
            </h2>
            <div className="mt-4 flex flex-wrap gap-3">
              <Badge tone="info">{contract?.status || "Unknown"}</Badge>
              <Badge>{contract?.documentTypeName || "Document"}</Badge>
              <Badge>{contract?.partnerName || "Partner"}</Badge>
            </div>
          </div>
          <div className="flex flex-wrap gap-3">
            {contractId && (
              <Button asChild variant="outline">
                <a href={previewUrl ?? "#"} download>
                  <Download className="h-4 w-4" />
                  Download file
                </a>
              </Button>
            )}
            {canRunAi && (
              <Button onClick={runAnalysis} disabled={processing || !contract?.currentVersionId}>
                {processing ? <LoaderCircle className="h-4 w-4 animate-spin" /> : <Sparkles className="h-4 w-4" />}
                {processing ? "Running AI..." : "Run AI review"}
              </Button>
            )}
          </div>
        </div>

        <div className="mt-8 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <Metric tone="blue" title="Effective date" value={formatDate(contract?.effectiveDate)} />
          <Metric tone="emerald" title="Expired date" value={formatDate(contract?.expiredDate)} />
          <Metric tone="amber" title="Value" value={formatCurrency(contract?.totalValue, contract?.currency)} />
          <Metric tone="violet" title="Version" value={contract?.currentVersionNumber ? `v${contract.currentVersionNumber}` : "N/A"} />
        </div>
      </Card>

      {processingJob && (
        <Card className={processingJob.status.toUpperCase() === "FAILED" ? "border-rose-200 bg-rose-50" : "border-brand-100 bg-brand-50/60"}>
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p className="font-semibold text-slate-900">AI processing job #{processingJob.queueId}</p>
              <p className="mt-1 text-sm text-slate-600">
                OCR, signature detection, metadata, clauses, summary, risk analysis, and retrieval indexing.
              </p>
            </div>
            <Badge tone={processingJob.status.toUpperCase() === "COMPLETED" ? "success" : processingJob.status.toUpperCase() === "FAILED" ? "danger" : "info"}>
              {processingJob.status.toUpperCase()}
            </Badge>
          </div>
          <div className="mt-4 h-2 overflow-hidden rounded-full bg-slate-200" role="progressbar" aria-label="AI processing progress" aria-valuemin={0} aria-valuemax={100} aria-valuenow={processingJob.progress ?? 0}>
            <div className="h-full rounded-full bg-brand-600 transition-[width]" style={{ width: `${Math.max(0, Math.min(100, processingJob.progress ?? 0))}%` }} />
          </div>
          <div className="mt-2 flex items-center justify-between text-xs text-slate-500">
            <span>{processingJob.status.toUpperCase() === "RUNNING" ? "AI Server is processing the document. You can keep this page open." : "Job status"}</span>
            <span>{processingJob.progress ?? 0}%</span>
          </div>
          {processingJob.errorMessage && (
            <p className="mt-4 rounded-2xl border border-rose-200 bg-white px-4 py-3 text-sm font-medium text-rose-700">
              {processingJob.errorMessage}
            </p>
          )}
        </Card>
      )}

      <div className="grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
        <Card className="overflow-hidden p-0">
          <div className="border-b border-slate-100 px-6 py-4">
            <h3 className="text-lg font-semibold text-slate-900">Document preview</h3>
          </div>
          {previewUrl ? (
            <iframe className="h-[760px] w-full bg-slate-50" src={previewUrl} title="Contract preview" />
          ) : (
            <div className="flex h-[760px] items-center justify-center bg-slate-50 text-slate-500">
              Preview is unavailable for this file.
            </div>
          )}
        </Card>

        <div className="space-y-4">
          <Card className="border-brand-100 bg-gradient-to-br from-brand-50 to-white">
            <div className="flex items-center gap-3">
              <span className="flex h-10 w-10 items-center justify-center rounded-2xl bg-brand-100 text-brand-700"><Bot className="h-5 w-5" /></span>
              <h3 className="text-lg font-semibold text-slate-900">AI summary</h3>
            </div>
            <p className="mt-4 rounded-2xl border border-brand-100 bg-white/80 px-4 py-3 text-sm font-medium leading-7 text-slate-700">{summary?.summary || "No summary available yet."}</p>
          </Card>

          <Card className="border-amber-100 bg-gradient-to-br from-amber-50 to-white">
            <div className="flex items-center gap-3">
              <span className="flex h-10 w-10 items-center justify-center rounded-2xl bg-amber-100 text-amber-700"><ShieldCheck className="h-5 w-5" /></span>
              <h3 className="text-lg font-semibold text-slate-900">Risk analysis</h3>
            </div>
            <div className="mt-4 flex items-center justify-between rounded-2xl border border-amber-100 bg-white/80 px-4 py-3">
              <span className="text-sm font-medium text-slate-700">Level</span>
              <Badge tone={risk?.riskLevel === "High" || risk?.riskLevel === "Critical" ? "danger" : "warning"}>
                {risk?.riskLevel || "Not analyzed"}
              </Badge>
            </div>
            <p className="mt-3 text-sm font-medium leading-7 text-slate-700">{risk?.riskSummary || "No risk summary available yet."}</p>
          </Card>

          <Card className="border-violet-100 bg-gradient-to-br from-violet-50/80 to-white">
            <div className="flex items-center gap-3">
              <span className="flex h-10 w-10 items-center justify-center rounded-2xl bg-violet-100 text-violet-700"><ScanSearch className="h-5 w-5" /></span>
              <h3 className="text-lg font-semibold text-slate-900">Extracted metadata</h3>
            </div>
            <div className="mt-4 space-y-3">
              {metadata.map((item) => (
                <div key={item.metadataId} className="rounded-[22px] border border-violet-100 bg-white/85 px-4 py-3">
                  <div className="flex items-center justify-between gap-4">
                    <p className="font-medium text-slate-800">{item.fieldName}</p>
                    <Badge tone={item.verified ? "success" : "warning"}>{item.verified ? "Verified" : "Pending"}</Badge>
                  </div>
                  <Input
                    className="mt-3 bg-white"
                    value={metadataDraft[item.metadataId] ?? valueToText(item.currentValue)}
                    onChange={(event) => setMetadataDraft((current) => ({ ...current, [item.metadataId]: event.target.value }))}
                  />
                  {item.confidence != null && <p className="mt-2 text-xs text-slate-500">AI confidence: {(item.confidence * 100).toFixed(0)}%</p>}
                </div>
              ))}
              {metadata.length === 0 && (
                <p className="rounded-[22px] border border-dashed border-violet-200 bg-white/70 px-4 py-6 text-center text-sm font-medium text-slate-600">
                  Run AI review to extract contract metadata.
                </p>
              )}
            </div>
            {metadata.length > 0 && canUpdateContract && (
              <Button className="mt-4 w-full" variant="outline" onClick={saveReviewedMetadata} disabled={savingMetadata}>
                {savingMetadata ? <LoaderCircle className="h-4 w-4 animate-spin" /> : <ShieldCheck className="h-4 w-4" />}
                Save reviewed metadata
              </Button>
            )}
          </Card>
        </div>
      </div>

      <Card>
        <div className="flex items-center gap-3">
          <ScanSearch className="h-5 w-5 text-brand-600" />
          <div>
            <h3 className="text-lg font-semibold text-slate-900">AI evidence</h3>
            <p className="mt-1 text-sm text-slate-500">OCR text and regions returned by the current contract version.</p>
          </div>
        </div>
        <div className="mt-5 grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
          <div className="max-h-80 overflow-auto rounded-[24px] border border-slate-100 bg-slate-50/70 p-4">
            {ocrResults.length === 0 ? (
              <p className="text-sm text-slate-500">Run AI review to generate OCR evidence.</p>
            ) : ocrResults.map((result) => (
              <section key={result.ocrId} className="border-b border-slate-200 py-3 last:border-0">
                <div className="flex items-center justify-between gap-4">
                  <p className="text-sm font-semibold text-slate-900">Page {result.pageNumber}</p>
                  <span className="text-xs text-slate-500">{result.confidence != null ? `${(result.confidence * 100).toFixed(0)}% confidence` : result.engine}</span>
                </div>
                <p className="mt-2 whitespace-pre-wrap text-sm leading-6 text-slate-600">{result.ocrText}</p>
              </section>
            ))}
          </div>
          <div className="rounded-[24px] border border-slate-100 bg-slate-50/70 p-4">
            <p className="text-sm font-semibold text-slate-900">Detected regions</p>
            <div className="mt-3 space-y-2">
              {detectionRegions.length === 0 ? (
                <p className="text-sm text-slate-500">No signature or layout region has been detected yet.</p>
              ) : detectionRegions.map((region) => (
                <div key={region.regionId} className="rounded-2xl bg-white px-3 py-3 text-sm">
                  <div className="flex items-center justify-between gap-3">
                    <span className="font-semibold text-slate-900">{region.label}</span>
                    <span className="text-xs text-slate-500">Page {region.pageNumber}</span>
                  </div>
                  <p className="mt-1 text-xs text-slate-500">Confidence: {region.confidence != null ? `${(region.confidence * 100).toFixed(0)}%` : "N/A"}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </Card>

      <Card>
        <div className="flex items-center gap-3">
          <FileText className="h-5 w-5 text-brand-600" />
          <div>
            <h3 className="text-lg font-semibold text-slate-900">Extracted clauses</h3>
            <p className="mt-1 text-sm text-slate-500">Key clauses identified from the current OCR result.</p>
          </div>
        </div>
        {clauses.length === 0 ? (
          <p className="mt-5 rounded-[22px] border border-dashed border-slate-200 bg-slate-50 px-4 py-5 text-sm text-slate-500">
            Run AI review to extract payment, confidentiality, termination, and other contract clauses.
          </p>
        ) : (
          <div className="mt-5 grid gap-3 lg:grid-cols-2">
            {clauses.map((clause) => (
              <article key={clause.clauseId} className="rounded-[24px] border border-slate-100 bg-slate-50/70 p-4">
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <p className="font-semibold text-slate-900">{clause.title}</p>
                    <p className="mt-1 text-xs uppercase tracking-[0.16em] text-brand-600">{clause.clauseType}</p>
                  </div>
                  {clause.pageNumber != null && <Badge>Page {clause.pageNumber}</Badge>}
                </div>
                <p className="mt-3 line-clamp-5 whitespace-pre-wrap text-sm leading-6 text-slate-600">{clause.clauseText}</p>
                {clause.matchedKeywords && clause.matchedKeywords.length > 0 && (
                  <div className="mt-4 flex flex-wrap gap-2">
                    {clause.matchedKeywords.map((keyword) => <Badge key={keyword} tone="info">{keyword}</Badge>)}
                  </div>
                )}
              </article>
            ))}
          </div>
        )}
      </Card>

      <div className="grid gap-4 xl:grid-cols-[0.9fr_1.1fr]">
        <Card>
          <div className="flex items-center gap-3">
            <MessageSquare className="h-5 w-5 text-brand-600" />
            <h3 className="text-lg font-semibold text-slate-900">Comments</h3>
          </div>
          <div className="mt-4 space-y-4">
            {groupedComments.map((comment) => (
              <div key={comment.commentId} className="rounded-[24px] border border-slate-100 bg-slate-50/80 p-4">
                <div className="flex items-start justify-between gap-3"><div><p className="text-sm font-semibold text-slate-900">{comment.userName}</p><p className="mt-1 text-xs text-slate-500">{formatDate(comment.createdAt)}</p></div><div className="flex gap-2"><Button size="sm" variant="ghost" onClick={() => { setReplyingTo(replyingTo === comment.commentId ? null : comment.commentId); setReplyText(""); }}>Reply</Button>{comment.userId === profile?.userId && <><Button size="sm" variant="ghost" onClick={() => { setEditingCommentId(comment.commentId); setEditingCommentText(comment.content); }}>Edit</Button><Button size="sm" variant="ghost" className="text-rose-600 hover:bg-rose-50 hover:text-rose-700" onClick={() => deleteComment(comment.commentId)}>Delete</Button></>}</div></div>
                {editingCommentId === comment.commentId ? <div className="mt-3 space-y-2"><Textarea className="min-h-[90px] bg-white" value={editingCommentText} onChange={(event) => setEditingCommentText(event.target.value)} /><div className="flex gap-2"><Button size="sm" onClick={() => saveCommentEdit(comment.commentId)}>Save</Button><Button size="sm" variant="outline" onClick={() => setEditingCommentId(null)}>Cancel</Button></div></div> : <p className="mt-3 text-sm leading-7 text-slate-700">{comment.content}</p>}
                {comment.replies.length > 0 && (
                  <div className="mt-4 space-y-3 border-l-2 border-brand-100 pl-4">
                    {comment.replies.map((reply) => (
                      <div key={reply.commentId}>
                        <p className="text-sm font-medium text-slate-900">{reply.userName}</p>
                        <p className="mt-1 text-sm text-slate-600">{reply.content}</p>
                      </div>
                    ))}
                  </div>
                )}
                {replyingTo === comment.commentId && <div className="mt-4 space-y-2 rounded-2xl border border-brand-100 bg-white p-3"><Textarea className="min-h-[82px]" value={replyText} onChange={(event) => setReplyText(event.target.value)} placeholder="Write a reply..." /><div className="flex gap-2"><Button size="sm" onClick={() => replyToComment(comment.commentId)}>Reply</Button><Button size="sm" variant="outline" onClick={() => setReplyingTo(null)}>Cancel</Button></div></div>}
              </div>
            ))}
          </div>
          <div className="mt-6 space-y-3">
            <Textarea value={commentText} onChange={(event) => setCommentText(event.target.value)} placeholder="Leave a comment on this contract..." />
            <select className="min-h-24 w-full rounded-2xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-800 outline-none focus:border-brand-400 focus:ring-4 focus:ring-brand-100" multiple value={mentionedUserIds.map(String)} onChange={(event) => setMentionedUserIds(Array.from(event.target.selectedOptions, (option) => Number(option.value)))}>{users.map((user) => <option key={user.userId} value={user.userId}>{user.fullName} - {user.email}</option>)}</select>
            <p className="text-xs text-slate-500">Optional: select people to mention. They receive a contract notification.</p>
            <Button onClick={handleCommentSubmit}>Post comment</Button>
          </div>
        </Card>

        <Card>
          <h3 className="text-lg font-semibold text-slate-900">Approval workflow</h3>
          <div className="mt-5 space-y-3">
            {workflow.map((step) => (
              <div key={step.workflowId} className="rounded-[24px] border border-slate-100 bg-slate-50/80 p-4">
                <div className="flex items-center justify-between gap-4">
                  <div>
                    <p className="font-semibold text-slate-900">Step {step.stepNumber}</p>
                    <p className="text-sm text-slate-500">{step.approverName}</p>
                  </div>
                  <Badge tone={step.status === "Approved" ? "success" : step.status === "Rejected" ? "danger" : "info"}>
                    {step.status}
                  </Badge>
                </div>
                {step.comment && <p className="mt-3 text-sm text-slate-600">{step.comment}</p>}
              </div>
            ))}
          </div>
          {workflow.length === 0 && canApprove && (
            <div className="mt-5 rounded-[24px] border border-dashed border-brand-200 bg-brand-50/60 p-4">
              <p className="text-sm font-semibold text-slate-900">Submit for approval</p>
              <p className="mt-1 text-sm text-slate-600">Choose approvers in the order they should review the contract.</p>
              <select
                className="mt-4 min-h-28 w-full rounded-2xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-800 outline-none focus:border-brand-400 focus:ring-4 focus:ring-brand-100"
                multiple
                value={selectedApprovers.map(String)}
                onChange={(event) => setSelectedApprovers(Array.from(event.target.selectedOptions, (option) => Number(option.value)))}
              >
                {approvalCandidates.map((user) => <option key={user.userId} value={user.userId}>{user.fullName} - {user.roles.join(", ")}</option>)}
              </select>
              <Button className="mt-4 w-full" onClick={submitWorkflow} disabled={workflowSubmitting}>
                {workflowSubmitting && <LoaderCircle className="h-4 w-4 animate-spin" />}
                Submit workflow
              </Button>
            </div>
          )}
          {workflow.length > 0 && canTakeApprovalAction && (
            <div className="mt-5 rounded-[24px] border border-slate-100 bg-slate-50/70 p-4">
              <Textarea
                className="min-h-[92px] bg-white"
                placeholder="Optional approval or rejection comment..."
                value={workflowComment}
                onChange={(event) => setWorkflowComment(event.target.value)}
              />
              <div className="mt-3 grid grid-cols-2 gap-3">
                <Button variant="outline" onClick={() => takeWorkflowAction("reject")} disabled={workflowSubmitting}>Reject</Button>
                <Button onClick={() => takeWorkflowAction("approve")} disabled={workflowSubmitting}>
                  {workflowSubmitting && <LoaderCircle className="h-4 w-4 animate-spin" />}
                  Approve
                </Button>
              </div>
            </div>
          )}
          {workflow.length === 0 && !canApprove && (
            <p className="mt-5 rounded-[22px] border border-slate-200 bg-slate-50 px-4 py-5 text-sm text-slate-600">
              Only contract managers can create an approval workflow.
            </p>
          )}
          {workflow.length > 0 && !canTakeApprovalAction && activeApprovalStep && (
            <p className="mt-5 rounded-[22px] border border-brand-100 bg-brand-50 px-4 py-5 text-sm text-slate-700">
              Waiting for <span className="font-semibold">{activeApprovalStep.approverName}</span> to complete the current approval step.
            </p>
          )}
        </Card>
      </div>
    </div>
  );
}

const metricTone = {
  blue: "border-brand-200 bg-gradient-to-br from-brand-50 to-blue-100/70 text-brand-800",
  emerald: "border-emerald-200 bg-gradient-to-br from-emerald-50 to-emerald-100/70 text-emerald-800",
  amber: "border-amber-200 bg-gradient-to-br from-amber-50 to-amber-100/70 text-amber-800",
  violet: "border-violet-200 bg-gradient-to-br from-violet-50 to-violet-100/70 text-violet-800",
} as const;

function Metric({ title, value, tone }: { title: string; value: string; tone: keyof typeof metricTone }) {
  return (
    <div className={`rounded-[24px] border px-4 py-4 shadow-sm ${metricTone[tone]}`}>
      <p className="text-sm font-semibold opacity-80">{title}</p>
      <p className="mt-2 text-xl font-bold">{value}</p>
    </div>
  );
}

function valueToText(value: unknown) {
  if (typeof value === "string") {
    return value;
  }
  return value == null ? "" : JSON.stringify(value);
}

async function waitForProcessingJob(initialJob: ProcessingJob, token: string) {
  let currentJob = initialJob;
  for (let attempt = 0; attempt < 300; attempt += 1) {
    const status = currentJob.status.toUpperCase();
    if (status === "COMPLETED" || status === "FAILED") {
      return currentJob;
    }

    await new Promise((resolve) => window.setTimeout(resolve, 2000));
    currentJob = await apiRequest<ProcessingJob>(
      `/api/v1/processing/jobs/${currentJob.queueId}`,
      {},
      token,
    );
  }

  throw new Error("AI processing is taking longer than expected. The job continues in the background.");
}
