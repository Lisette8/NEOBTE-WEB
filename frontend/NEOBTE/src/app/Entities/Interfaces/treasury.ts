export interface FraisEntry {
  id: number;
  virementId: number;
  montantFrais: number;
  tauxApplique: number;
  montantVirement: number;
  senderName: string;
  recipientName: string;
  date: string;
}

export interface Treasury {
  // Revenue
  totalCollected: number;
  feeRate: number;
  // Investment pool
  investmentPool: number;
  reserves: number;
  deployed: number;
  reserveRate: number;
  // Investment stats
  activeInvestments: number;
  totalInterestPaid: number;
  // Audit
  recentTransactions: FraisEntry[];
}
  