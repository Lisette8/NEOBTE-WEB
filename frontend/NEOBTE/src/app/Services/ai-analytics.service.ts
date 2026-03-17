import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AnalyticsData, AiInsights, ChatMessage, ChatResponse } from '../Entities/Interfaces/ai-analytics';

@Injectable({ providedIn: 'root' })
export class AiAnalyticsService {

  private readonly base = 'http://localhost:8080/api/v1/admin/ai';

  constructor(private http: HttpClient) {}

  getAnalytics(): Observable<AnalyticsData> {
    return this.http.get<AnalyticsData>(`${this.base}/analytics`);
  }

  getInsights(): Observable<AiInsights> {
    return this.http.get<AiInsights>(`${this.base}/insights`);
  }

  chat(message: string, history: ChatMessage[]): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${this.base}/chat`, { message, history });
  }
}
