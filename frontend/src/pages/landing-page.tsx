import { ArrowRight, Bot, Building2, CheckCircle2, FileSearch, ShieldCheck, Sparkles } from "lucide-react";
import { Link } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";

const features = [
  {
    icon: FileSearch,
    title: "OCR and metadata extraction",
    description: "Upload contracts, detect layout, extract key fields, and keep a verified record in PostgreSQL.",
  },
  {
    icon: Bot,
    title: "RAG contract assistant",
    description: "Ask about expiry, payment terms, confidentiality, and risk signals directly from stored documents.",
  },
  {
    icon: ShieldCheck,
    title: "Approval and collaboration",
    description: "Comment, mention colleagues, review metadata, and route contracts through controlled approval steps.",
  },
];

export function LandingPage() {
  return (
    <div className="min-h-screen bg-[#f6eee6] text-[#8f201c]">
      <div className="mx-auto max-w-[1440px] px-4 py-4 md:px-8">
        <div className="overflow-hidden rounded-[38px] border border-[#d89a92] bg-[#f8f0e7] shadow-[0_24px_60px_rgba(143,32,28,0.12)]">
          <header className="border-b border-[#d89a92] px-6 py-5 md:px-10">
            <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
              <div>
                <p className="font-display text-3xl font-semibold tracking-tight">IDP Vision</p>
                <p className="text-sm text-[#8f201c]/70">Contract intelligence platform for enterprise operations</p>
              </div>
              <nav className="flex flex-wrap items-center gap-4 text-sm font-medium">
                <a href="#gioi-thieu" className="hover:opacity-70">About</a>
                <a href="#chuc-nang" className="hover:opacity-70">Features</a>
                <a href="#quy-trinh" className="hover:opacity-70">Workflow</a>
                <a href="#cong-nghe" className="hover:opacity-70">Technology</a>
                <a href="#faq" className="hover:opacity-70">FAQ</a>
              </nav>
              <Link to="/login">
                <Button className="rounded-full bg-[#ba2720] hover:bg-[#8f201c]">
                  Dang nhap
                  <ArrowRight className="h-4 w-4" />
                </Button>
              </Link>
            </div>
          </header>

          <section className="hero-grid px-6 py-12 md:px-10 md:py-20">
            <div className="grid gap-12 md:grid-cols-[1.25fr_0.75fr] md:items-center">
              <div>
                <p className="text-sm uppercase tracking-[0.35em] text-[#ba2720]/70">Hero</p>
                <h1 className="font-display text-5xl font-semibold leading-none md:text-7xl">
                  From contract upload to AI decision support
                </h1>
                <p className="mt-6 max-w-2xl text-lg leading-8 text-[#8f201c]/80">
                  The platform combines React, Spring Boot, FastAPI, PostgreSQL, OCR pipelines, RAG chat, and approval flows
                  so teams can process real contracts faster and with stronger governance.
                </p>
                <div className="mt-8 flex flex-wrap gap-4">
                  <Link to="/login">
                    <Button className="rounded-full bg-[#ba2720] hover:bg-[#8f201c]">Open system</Button>
                  </Link>
                  <a href="#lien-he">
                    <Button variant="outline" className="rounded-full border-[#d89a92] bg-transparent text-[#8f201c] hover:bg-[#f3e2d7]">
                      Contact us
                    </Button>
                  </a>
                </div>
              </div>
              <Card className="border-[#d89a92] bg-[#fff8f3] p-7 text-[#8f201c] shadow-[0_30px_60px_rgba(143,32,28,0.12)]">
                <div className="grid gap-4">
                  <div className="rounded-[30px] bg-[#ba2720] p-5 text-white">
                    <p className="text-sm uppercase tracking-[0.24em]">Workflow</p>
                    <p className="mt-2 text-2xl font-semibold">Upload → OCR → Metadata → RAG → Approval</p>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="rounded-[26px] border border-[#d89a92] p-4">
                      <Sparkles className="h-6 w-6" />
                      <p className="mt-4 font-semibold">LLM summary</p>
                      <p className="mt-2 text-sm text-[#8f201c]/70">Readable summaries and structured insights.</p>
                    </div>
                    <div className="rounded-[26px] border border-[#d89a92] p-4">
                      <Building2 className="h-6 w-6" />
                      <p className="mt-4 font-semibold">Enterprise control</p>
                      <p className="mt-2 text-sm text-[#8f201c]/70">Roles, approvals, comments, and audit-friendly flow.</p>
                    </div>
                  </div>
                </div>
              </Card>
            </div>
          </section>

          <section id="gioi-thieu" className="border-y border-[#d89a92] bg-[#ba2720] px-6 py-5 text-white md:px-10">
            <div className="grid gap-4 md:grid-cols-4">
              <p className="font-display text-4xl">Created for legal, operations, and business teams</p>
              <p className="md:col-span-3 text-sm leading-7 text-white/80">
                Intelligent Document Processing helps organizations centralize contracts, extract real information from
                uploaded files, validate metadata, and keep every approval step visible in one place.
              </p>
            </div>
          </section>

          <section id="chuc-nang" className="px-6 py-14 md:px-10">
            <div className="mb-8 flex items-end justify-between gap-6">
              <div>
                <p className="text-sm uppercase tracking-[0.3em] text-[#ba2720]/70">Features</p>
                <h2 className="font-display text-5xl">Core capabilities</h2>
              </div>
            </div>
            <div className="grid gap-6 md:grid-cols-3">
              {features.map((feature) => {
                const Icon = feature.icon;
                return (
                  <Card key={feature.title} className="border-[#e6c4bd] bg-[#fffaf6] text-[#8f201c]">
                    <Icon className="h-8 w-8" />
                    <h3 className="mt-6 text-2xl font-semibold">{feature.title}</h3>
                    <p className="mt-3 leading-7 text-[#8f201c]/75">{feature.description}</p>
                  </Card>
                );
              })}
            </div>
          </section>

          <section id="quy-trinh" className="bg-[#fff8f3] px-6 py-14 md:px-10">
            <p className="text-sm uppercase tracking-[0.3em] text-[#ba2720]/70">Workflow</p>
            <h2 className="font-display text-5xl">Processing flow</h2>
            <div className="mt-10 grid gap-4 md:grid-cols-5">
              {["Upload", "OCR & Layout", "Metadata & Summary", "RAG Chat", "Approval"].map((step, index) => (
                <div key={step} className="rounded-[28px] border border-[#e0b4aa] bg-white p-5">
                  <p className="text-sm uppercase tracking-[0.3em] text-[#ba2720]/60">0{index + 1}</p>
                  <p className="mt-5 text-xl font-semibold">{step}</p>
                </div>
              ))}
            </div>
          </section>

          <section id="cong-nghe" className="px-6 py-14 md:px-10">
            <div className="grid gap-10 md:grid-cols-2">
              <div>
                <p className="text-sm uppercase tracking-[0.3em] text-[#ba2720]/70">Technology</p>
                <h2 className="font-display text-5xl">Built for modular growth</h2>
                <ul className="mt-8 grid gap-4">
                  {["React + Vite + TypeScript", "Spring Boot + JWT security", "FastAPI AI service", "PostgreSQL + vector-ready storage", "YOLO / OCR / RAG friendly model interfaces"].map(
                    (item) => (
                      <li key={item} className="flex items-start gap-3 text-lg">
                        <CheckCircle2 className="mt-1 h-5 w-5 shrink-0" />
                        <span>{item}</span>
                      </li>
                    ),
                  )}
                </ul>
              </div>
              <Card className="border-[#e6c4bd] bg-[#fffaf6] text-[#8f201c]">
                <h3 className="text-2xl font-semibold">Application domains</h3>
                <div className="mt-6 grid gap-4 sm:grid-cols-2">
                  {["Procurement", "Legal review", "Sales contracts", "HR agreements", "Partner onboarding", "Operations compliance"].map(
                    (item) => (
                      <div key={item} className="rounded-[22px] border border-[#ecd1cb] px-4 py-4 font-medium">
                        {item}
                      </div>
                    ),
                  )}
                </div>
              </Card>
            </div>
          </section>

          <section id="lien-he" className="border-t border-[#d89a92] bg-[#fff8f3] px-6 py-14 md:px-10">
            <div className="grid gap-10 md:grid-cols-[1.2fr_0.8fr]">
              <div>
                <p className="text-sm uppercase tracking-[0.3em] text-[#ba2720]/70">Contact</p>
                <h2 className="font-display text-5xl">Start with a system walkthrough</h2>
                <p className="mt-5 max-w-2xl leading-8 text-[#8f201c]/80">
                  Use the login entry point to move into the internal workspace, test the processing flow, and prepare the
                  environment for enterprise deployment.
                </p>
              </div>
              <Card className="border-[#d89a92] bg-[#ba2720] text-white">
                <h3 className="text-2xl font-semibold">Ready to explore?</h3>
                <p className="mt-4 text-sm leading-7 text-white/80">The internal dashboard, contract pages, comments, and RAG chat are connected from a single app shell.</p>
                <Link to="/login" className="mt-6 inline-flex">
                  <Button className="rounded-full bg-white text-[#8f201c] hover:bg-[#f3e2d7]">Dang nhap vao he thong</Button>
                </Link>
              </Card>
            </div>
          </section>

          <section id="faq" className="px-6 py-14 md:px-10">
            <p className="text-sm uppercase tracking-[0.3em] text-[#ba2720]/70">FAQ</p>
            <h2 className="font-display text-5xl">Common questions</h2>
            <div className="mt-8 grid gap-4">
              {[
                "Can the system support enterprise approval flow and collaboration? Yes, comments, mentions, notifications, and approval steps are part of the core workspace.",
                "Does React call the AI server directly? No. The backend remains the central orchestration layer.",
                "Can AI services be swapped later? Yes. The FastAPI service is organized with pluggable model and service layers.",
              ].map((item) => (
                <Card key={item} className="border-[#e6c4bd] bg-[#fffaf6] text-[#8f201c]">
                  <p className="leading-7">{item}</p>
                </Card>
              ))}
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}
