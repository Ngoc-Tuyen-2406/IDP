export type ApiEnvelope<T> = {
  success: boolean;
  message: string;
  data: T;
  errors?: unknown;
  timestamp?: string;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export type LoginResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: {
    userId: number;
    email: string;
    fullName: string;
    status: string;
    emailVerified: boolean;
    roles: string[];
    permissions: string[];
  };
};

export type DashboardStatistics = {
  totalContracts: number;
  draftContracts: number;
  processingContracts: number;
  pendingContracts: number;
  approvedContracts: number;
  rejectedContracts: number;
  expiringContracts: number;
};

export type StatusMetric = {
  status: string;
  count: number;
};

export type ContractSummary = {
  contractId: number;
  contractNumber: string;
  contractName: string;
  documentTypeName: string;
  partnerName: string;
  status: string;
  effectiveDate?: string | null;
  expiredDate?: string | null;
  totalValue?: number | null;
  currency?: string | null;
  latestFileName?: string | null;
  favorite: boolean;
  createdAt: string;
  updatedAt: string;
};

export type ContractDetail = ContractSummary & {
  documentTypeId: number;
  partnerId: number;
  uploadedById: number;
  uploadedByName: string;
  partnerRepresentativeName?: string | null;
  partnerRepresentativePosition?: string | null;
  signedDate?: string | null;
  currentVersionId?: number | null;
  currentVersionNumber?: number | null;
  versions: Array<{
    versionId: number;
    versionNumber: number;
    status: string;
    changeNote?: string | null;
    isCurrent: boolean;
    createdAt: string;
  }>;
  files: Array<{
    fileId: number;
    fileName: string;
    fileType?: string | null;
    fileSize?: number | null;
    pageCount?: number | null;
    fileHash?: string | null;
    uploadedAt: string;
  }>;
};

export type DashboardResponseBundle = {
  statistics?: DashboardStatistics;
  statusMetrics?: StatusMetric[];
  recentContracts?: PageResponse<ContractSummary>;
};

export type MetadataField = {
  metadataId: number;
  versionId?: number;
  fieldName: string;
  fieldType?: string | null;
  currentValue: unknown;
  confidence?: number | null;
  verified: boolean;
};

export type OcrResult = {
  ocrId: number;
  versionId: number;
  pageNumber: number;
  language?: string | null;
  engine?: string | null;
  ocrText: string;
  confidence?: number | null;
  createdAt: string;
};

export type DetectionRegion = {
  regionId: number;
  versionId: number;
  pageNumber: number;
  label: string;
  xMin: number;
  yMin: number;
  xMax: number;
  yMax: number;
  confidence?: number | null;
};

export type ContractClause = {
  clauseId: number;
  versionId: number;
  clauseType: string;
  title: string;
  clauseText: string;
  matchedKeywords?: string[] | null;
  pageNumber?: number | null;
  confidence?: number | null;
  modelName?: string | null;
  createdAt: string;
};

export type ProcessingJob = {
  queueId: number;
  versionId: number;
  contractId: number;
  taskType: string;
  status: string;
  progress?: number | null;
  errorMessage?: string | null;
  createdAt: string;
  startedAt?: string | null;
  finishedAt?: string | null;
};

export type SummaryResponse = {
  summaryId: number;
  versionId: number;
  summary: string;
  summaryJson?: unknown;
  modelName?: string | null;
  createdAt: string;
};

export type RiskResponse = {
  riskId: number;
  versionId: number;
  riskLevel: string;
  riskScore?: number | null;
  riskSummary?: string | null;
  recommendation?: string | null;
  riskDetails?: unknown;
  modelName?: string | null;
  createdAt: string;
};

export type CommentItem = {
  commentId: number;
  contractId: number;
  parentCommentId?: number | null;
  userId: number;
  userName: string;
  content: string;
  pageNumber?: number | null;
  createdAt: string;
  mentionedUserIds: number[];
};

export type WorkflowStep = {
  workflowId: number;
  contractId: number;
  stepNumber: number;
  approverId: number;
  approverName: string;
  status: string;
  comment?: string | null;
  createdAt: string;
  approvedAt?: string | null;
};

export type NotificationItem = {
  notificationId: number;
  title: string;
  content: string;
  type: string;
  link?: string | null;
  isRead: boolean;
  createdAt: string;
};

export type NotificationSettings = {
  userId: number;
  emailEnabled: boolean;
  systemNotification: boolean;
  contractNew: boolean;
  contractApproval: boolean;
  contractExpiring: boolean;
  commentMention: boolean;
  updatedAt?: string | null;
};

export type UserProfile = {
  userId: number;
  fullName: string;
  email: string;
  phone?: string | null;
  avatar?: string | null;
  departmentId?: number | null;
  departmentName?: string | null;
  status: string;
  emailVerified: boolean;
  lastLogin?: string | null;
  roles: string[];
  permissions: string[];
};

export type UserSession = {
  sessionId: number;
  deviceName?: string | null;
  ipAddress?: string | null;
  createdAt: string;
  expiresAt: string;
  revoked: boolean;
  expired: boolean;
};

export type UserItem = {
  userId: number;
  departmentId?: number | null;
  departmentName?: string | null;
  fullName: string;
  email: string;
  phone?: string | null;
  avatar?: string | null;
  status: string;
  emailVerified: boolean;
  roles: string[];
};

export type RoleItem = {
  roleId: number;
  roleName: string;
  description?: string | null;
};

export type PermissionItem = {
  permissionId: number;
  permissionName: string;
  description?: string | null;
};

export type RoleDetail = RoleItem & {
  permissions: PermissionItem[];
};

export type DepartmentItem = {
  departmentId: number;
  departmentName: string;
  description?: string | null;
};

export type PartnerItem = {
  partnerId: number;
  companyName: string;
  partnerType?: string | null;
  taxCode?: string | null;
  email?: string | null;
  phone?: string | null;
  website?: string | null;
  address?: string | null;
};

export type DocumentTypeItem = {
  documentTypeId: number;
  name: string;
  description?: string | null;
};

export type ChatMessage = {
  chatId: number;
  versionId: number;
  contractId: number;
  question: string;
  answer: string;
  responseTimeMs?: number | null;
  conversationId: string;
  sourceChunkIds: number[];
  createdAt: string;
};

export type ChatContextResponse = {
  contractId: number;
  versionId: number;
  summary?: string | null;
  metadata: MetadataField[];
  chunks: string[];
};

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  constructor(message: string, public status?: number) {
    super(message);
  }
}

function authHeaders(token?: string) {
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
  token?: string,
): Promise<T> {
  const headers = new Headers(options.headers);
  if (!(options.body instanceof FormData) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let message = "Request failed";
    try {
      const body = (await response.json()) as ApiEnvelope<unknown>;
      message = body.message || message;
    } catch {
      message = response.statusText || message;
    }
    throw new ApiError(message, response.status);
  }

  const body = (await response.json()) as ApiEnvelope<T>;
  return body.data;
}

export async function downloadProtectedFile(path: string, token: string) {
  const headers = new Headers();
  headers.set("Authorization", `Bearer ${token}`);

  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers,
  });

  if (!response.ok) {
    throw new ApiError("Khong the tai file", response.status);
  }

  return response.blob();
}

export const apiBaseUrl = API_BASE_URL;
