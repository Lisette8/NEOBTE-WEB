import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { RegisterRequest } from '../Entities/Interfaces/register-request';
import { AuthResponse } from '../Entities/Interfaces/auth-response';
import { LoginRequest } from '../Entities/Interfaces/login-request';
import { Treasury } from '../Entities/Interfaces/treasury';
import { Rib } from '../Entities/Interfaces/rib';
import { ChangePasswordRequest, ClientProfile, UpdateClientProfileRequest } from '../Entities/Interfaces/client-profile';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private API_URL    = 'http://localhost:8080/api/v1/auth';
  private CLIENT_URL = 'http://localhost:8080/api/v1/client';
  private ADMIN_URL  = 'http://localhost:8080/api/v1/admin';
 
  constructor(private http: HttpClient) {}
 
  login(data: LoginRequest) {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, data).pipe(
      tap(res => localStorage.setItem('token', res.token))
    );
  }
 
  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, data).pipe(
      tap(res => localStorage.setItem('token', res.token))
    );
  }
 
  logout(): Observable<any> {
    return this.http.post(`${this.API_URL}/logout`, {}, { responseType: 'text' }).pipe(
      tap(() => localStorage.removeItem('token'))
    );
  }
 
  // ── Password Reset ──
  forgotPassword(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/forgot-password`, { email });
  }
 
  verifyResetCode(email: string, code: string): Observable<{ resetToken: string }> {
    return this.http.post<{ resetToken: string }>(`${this.API_URL}/verify-reset-code`, { email, code });
  }
 
  resetPassword(resetToken: string, newPassword: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/reset-password`, { resetToken, newPassword });
  }
 
  // ── Profile ──
  getCurrentUser(): Observable<ClientProfile> {
    return this.http.get<ClientProfile>(`${this.CLIENT_URL}/current`);
  }

  updateProfile(dto: UpdateClientProfileRequest): Observable<ClientProfile> {
    return this.http.put<ClientProfile>(`${this.CLIENT_URL}/current`, dto);
  }

  changePassword(dto: ChangePasswordRequest): Observable<string> {
    return this.http.put(`${this.CLIENT_URL}/change-password`, dto, { responseType: 'text' });
  }

  uploadProfilePhoto(image: File): Observable<ClientProfile> {
    const fd = new FormData();
    fd.append('image', image);
    return this.http.put<ClientProfile>(`${this.CLIENT_URL}/photo`, fd);
  }

  getMyRib(): Observable<Rib> {
    return this.http.get<Rib>(`${this.CLIENT_URL}/rib`);
  }
 
  getTreasury(): Observable<Treasury> {
    return this.http.get<Treasury>(`${this.ADMIN_URL}/treasury`);
  }
 
  getToken(): string | null { return localStorage.getItem('token'); }
 
  getUserId(): number | null {
    const token = this.getToken();
    if (!token) return null;
    try { return Number(JSON.parse(atob(token.split('.')[1])).sub); }
    catch { return null; }
  }
 
  getUserRole(): string | null {
    const token = this.getToken();
    if (!token) return null;
    try { return JSON.parse(atob(token.split('.')[1])).role; }
    catch { return null; }
  }
 
  isLoggedIn(): boolean { return this.getToken() != null; }
}
