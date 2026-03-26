/**
 * Per-account daily/monthly usage returned by /api/v1/client/ai/premium/status
 * Transfer limits are now account-based, not profile-based.
 */
export interface AccountUsage {
  compteId: number;
  typeCompte: 'COURANT' | 'EPARGNE' | 'PROFESSIONNEL';
  dailyCountUsed: number;
  dailyCountLimit: number;
  monthlyCountUsed: number;
  monthlyCountLimit: number;
  dailyAmountLimit: number;
  maxTransferAmount: number;
  canSendExternal: boolean;
}

/**
 * Response from /premium/status.
 * premium: gates AI chatbot and live market data — NOT transfer limits.
 */
export interface PremiumStatus {
  premium: boolean;
  accountUsages: AccountUsage[];
}

export interface ClientInsightsData {
  monthlyTransfers: {
    labels: string[];
    sent: number[];
    received: number[];
  };
  dailyBalance: {
    labels: string[];
    values: number[];
  };
  summary: {
    sentThisMonth: number;
    receivedThisMonth: number;
    txCountThisMonth: number;
  };
  generatedAt: string;
}

export interface ClientChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface ClientChatRequest {
  message: string;
  history: ClientChatMessage[];
}

export interface ClientChatResponse {
  reply: string;
}
