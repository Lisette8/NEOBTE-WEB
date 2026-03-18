import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MarketRatesResponse } from '../Entities/Interfaces/market-rates';

@Injectable({ providedIn: 'root' })
export class ClientMarketService {
  private readonly base = 'http://localhost:8080/api/v1/client/market';

  constructor(private http: HttpClient) {}

  getRates(): Observable<MarketRatesResponse> {
    return this.http.get<MarketRatesResponse>(`${this.base}/rates`);
  }
}

