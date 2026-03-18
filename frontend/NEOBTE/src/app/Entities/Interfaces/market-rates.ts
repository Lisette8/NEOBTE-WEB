export interface FxRateItem {
  pair: string;   // ex: "EUR/TND"
  rate: number;   // ex: 3.41
}

export interface CryptoRateItem {
  symbol: string;      // BTC, ETH, ...
  priceUsd?: number | null;
  priceEur?: number | null;
  priceTnd?: number | null;
}

export interface MarketRatesResponse {
  generatedAt: string; // ISO
  stale: boolean;
  usdToTnd: number;
  fx: FxRateItem[];
  crypto: CryptoRateItem[];
}

