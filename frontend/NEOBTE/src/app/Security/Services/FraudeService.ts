import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { FraudeAlerte, FraudeConfig } from "../../Entities/Interfaces/fraude";


@Injectable({ providedIn: 'root' })
export class FraudeService {
 
  private api = 'http://localhost:8080/api/v1/admin/fraude';
 
  constructor(private http: HttpClient) {}
 
  getAllAlertes(): Observable<FraudeAlerte[]> {
    return this.http.get<FraudeAlerte[]>(`${this.api}/alertes`);
  }
 
  getOpenAlertes(): Observable<FraudeAlerte[]> {
    return this.http.get<FraudeAlerte[]>(`${this.api}/alertes/open`);
  }
 
  countOpen(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.api}/alertes/count-open`);
  }
 
  reviewAlerte(id: number, newStatut: string, adminNote?: string): Observable<FraudeAlerte> {
    return this.http.put<FraudeAlerte>(`${this.api}/alertes/${id}/review`, { newStatut, adminNote });
  }
 
  getConfig(): Observable<FraudeConfig> {
    return this.http.get<FraudeConfig>(`${this.api}/config`);
  }
 
  updateConfig(cfg: FraudeConfig): Observable<FraudeConfig> {
    return this.http.put<FraudeConfig>(`${this.api}/config`, cfg);
  }
}
 