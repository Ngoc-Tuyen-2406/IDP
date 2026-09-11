import { Bot, SendHorizontal, Sparkles } from "lucide-react";
import { useEffect, useState } from "react";

import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import { apiRequest, type ChatMessage, type ChatContextResponse, type ContractSummary, type PageResponse } from "@/lib/api";
import { useAuth } from "@/state/auth-store";

export function ChatPage() {
  const { token } = useAuth();
  const [contractId, setContractId] = useState("");
  const [question, setQuestion] = useState("");
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [context, setContext] = useState<ChatContextResponse | null>(null);
  const [contracts, setContracts] = useState<ContractSummary[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!token) {
      return;
    }
    Promise.allSettled([
      apiRequest<PageResponse<ChatMessage>>("/api/v1/chat/history", {}, token),
      apiRequest<PageResponse<ContractSummary>>("/api/v1/contracts?size=100", {}, token),
    ]).then(([history, contractPage]) => {
      setMessages(history.status === "fulfilled" ? history.value.content : []);
      setContracts(contractPage.status === "fulfilled" ? contractPage.value.content : []);
    });
  }, [token]);

  useEffect(() => {
    if (contractId) {
      loadContext().catch(() => setContext(null));
    } else {
      setContext(null);
    }
  }, [contractId]);

  async function loadContext() {
    if (!token || !contractId) {
      return;
    }
    const data = await apiRequest<ChatContextResponse>(
      "/api/v1/chat/context",
      {
        method: "POST",
        body: JSON.stringify({ contractId: Number(contractId) }),
      },
      token,
    );
    setContext(data);
  }

  async function ask() {
    if (!token || !contractId || !question.trim()) {
      return;
    }
    setLoading(true);
    try {
      const message = await apiRequest<ChatMessage>(
        "/api/v1/chat/ask",
        {
          method: "POST",
          body: JSON.stringify({
            contractId: Number(contractId),
            question,
          }),
        },
        token,
      );
      setMessages((current) => [message, ...current]);
      setQuestion("");
      await loadContext();
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="grid gap-4 xl:grid-cols-[0.88fr_1.12fr]">
      <Card className="relative overflow-hidden border-brand-500/20 bg-gradient-to-br from-brand-900 via-brand-700 to-cyan-600 text-white">
        <div className="pointer-events-none absolute -left-20 bottom-0 h-64 w-64 rounded-full bg-cyan-300/15 blur-2xl" />
        <div className="flex items-center justify-between">
          <div className="relative">
            <p className="text-sm font-semibold uppercase tracking-[0.24em] text-cyan-100">Chat RAG</p>
            <h2 className="mt-2 text-3xl font-semibold text-white">Contract robot assistant</h2>
          </div>
          <div className="animate-float rounded-[32px] border border-white/15 bg-white/10 p-4 backdrop-blur">
            <Bot className="h-16 w-16 text-brand-100" />
          </div>
        </div>
        <div className="mt-8 rounded-[28px] border border-white/15 bg-white/10 p-6 backdrop-blur">
          <div className="mx-auto flex w-44 flex-col items-center">
            <div className="flex h-24 w-24 items-center justify-center rounded-full bg-white/15 shadow-soft">
              <Bot className="h-12 w-12 text-white" />
            </div>
            <div className="mt-4 h-4 w-24 rounded-full bg-white/20" />
            <div className="mt-3 flex gap-3">
              <div className="h-3 w-3 rounded-full bg-cyan-300 shadow-[0_0_18px_rgba(125,211,252,0.95)]" />
              <div className="h-3 w-3 rounded-full bg-cyan-300 shadow-[0_0_18px_rgba(125,211,252,0.95)]" />
            </div>
            <div className="mt-6 h-24 w-40 rounded-[28px] border border-white/15 bg-white/10" />
          </div>
          <p className="relative mt-6 text-center text-sm font-medium leading-7 text-white/90">
            Ask this robot about expiry dates, payment terms, approval context, and extracted metadata from the selected contract.
          </p>
        </div>
      </Card>

      <div className="space-y-4">
        <Card>
          <div className="grid gap-3 md:grid-cols-[220px_1fr_auto]">
            <select
              className="h-11 rounded-2xl border border-slate-200 bg-white/85 px-4 text-sm text-slate-900 outline-none transition focus:border-brand-400 focus:ring-4 focus:ring-brand-100"
              value={contractId}
              onChange={(event) => setContractId(event.target.value)}
            >
              <option value="">Select a contract</option>
              {contracts.map((contract) => (
                <option key={contract.contractId} value={contract.contractId}>
                  {contract.contractNumber} - {contract.contractName || contract.partnerName}
                </option>
              ))}
            </select>
            <Textarea
              className="min-h-[72px]"
              placeholder="Ask a question about the contract..."
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
            />
            <Button className="self-end" onClick={ask} disabled={loading}>
              <SendHorizontal className="h-4 w-4" />
              {loading ? "Sending..." : "Ask"}
            </Button>
          </div>
        </Card>

        <Card>
          <div className="flex items-center gap-2">
            <Sparkles className="h-5 w-5 text-brand-600" />
            <h3 className="text-lg font-semibold text-slate-900">Context snapshot</h3>
          </div>
          <div className="mt-4 space-y-3">
            <p className="text-sm leading-7 text-slate-600">{context?.summary || "Summary will appear after choosing a contract and asking the assistant."}</p>
            {context?.chunks.map((chunk, index) => (
              <div key={index} className="rounded-[22px] border border-slate-100 bg-slate-50/80 px-4 py-3 text-sm text-slate-600">
                {chunk}
              </div>
            ))}
          </div>
        </Card>

        <Card>
          <h3 className="text-lg font-semibold text-slate-900">Conversation history</h3>
          <div className="mt-5 space-y-4">
            {messages.map((message) => (
              <div key={message.chatId} className="rounded-[24px] border border-slate-100 bg-slate-50/70 p-4">
                <p className="font-semibold text-slate-900">{message.question}</p>
                <p className="mt-3 text-sm leading-7 text-slate-600">{message.answer}</p>
                {message.sourceChunkIds.length > 0 && (
                  <div className="mt-4 flex flex-wrap gap-2">
                    {message.sourceChunkIds.map((chunkId) => (
                      <span key={chunkId} className="rounded-full bg-brand-100 px-3 py-1 text-xs font-semibold text-brand-700">
                        Source: Chunk {chunkId}
                      </span>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
}
