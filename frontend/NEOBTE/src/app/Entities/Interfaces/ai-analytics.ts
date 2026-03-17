export interface DailyStat {
  date: string;
  totalAmount: number;
  count: number;
}

export interface MonthlyStat {
  month: string;
  count: number;
}

export interface TypeStat {
  label: string;
  count: number;
}

export interface UserRisk {
  userId: number;
  nom: string;
  email: string;
  alertCount: number;
  highSeverityCount: number;
  riskLevel: 'HIGH' | 'MEDIUM' | 'LOW';
}

export interface AnalyticsData {
  totalTransfers: number;
  totalVolume: number;
  avgTransfer: number;
  totalClients: number;
  openFraudAlerts: number;

  dailyTransfers: DailyStat[];
  forecast: DailyStat[];
  monthlyUsers: MonthlyStat[];
  fraudByType: TypeStat[];
  fraudBySeverity: TypeStat[];
  fraudTrend: DailyStat[];
  highRiskUsers: UserRisk[];
}

export interface AiInsights {
  fraudSummary: string;
  financialInsights: string;
  topRecommendations: string[];
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface ChatRequest {
  message: string;
  history: ChatMessage[];
}

export interface ChatResponse {
  reply: string;
}
