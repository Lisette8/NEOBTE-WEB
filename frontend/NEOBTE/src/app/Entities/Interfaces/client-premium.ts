export interface PremiumStatus {
  premium: boolean;
  transfersThisMonth: number;
  monthlyLimit: number;
}

export interface ClientInsightsData {
  monthlyTransfers: {
    labels: string[];   // e.g. ["Oct 2025", ...]
    sent: number[];     // total amount sent
    received: number[]; // total amount received
  };
  dailyBalance: {
    labels: string[]; // e.g. ["01/03", ...]
    values: number[]; // balance at end of day
  };
  summary: {
    sentThisMonth: number;
    receivedThisMonth: number;
    txCountThisMonth: number;
  };
  generatedAt: string; // ISO date
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
