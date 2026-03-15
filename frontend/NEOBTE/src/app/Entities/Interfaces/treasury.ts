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
    totalCollected: number;
    feeRate: number;
    recentTransactions: FraisEntry[];
  }