import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
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

  private currentUserSubject = new BehaviorSubject<ClientProfile | null>(null);
  readonly currentUser$ = this.currentUserSubject.asObservable();

  private photoCacheBuster = Date.now();
  getPhotoCacheBuster(): number {
    return this.photoCacheBuster;
  }

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, data).pipe(
      tap(res => { if (res.token) localStorage.setItem('token', res.token); })
    );
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, data).pipe(
      tap(res => { if (res.token) localStorage.setItem('token', res.token); })
    );
  }

  logout(): Observable<any> {
    return this.http.post(`${this.API_URL}/logout`, {}, { responseType: 'text' }).pipe(
      tap(() => {
        localStorage.removeItem('token');
        this.currentUserSubject.next(null);
      })
    );
  }

  // ── Password Reset ──────────────────────────────────────────────────────
  forgotPassword(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/forgot-password`, { email });
  }

  verifyResetCode(email: string, code: string): Observable<{ resetToken: string }> {
    return this.http.post<{ resetToken: string }>(`${this.API_URL}/verify-reset-code`, { email, code });
  }

  resetPassword(resetToken: string, newPassword: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/reset-password`, { resetToken, newPassword });
  }

  // ── PIN 2FA (login flow — no JWT yet) ───────────────────────────────────
  verifyPin(pinTempToken: string, pin: string): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.API_URL}/verify-pin`, { pinTempToken, pin }).pipe(
      tap(res => { if (res.token) localStorage.setItem('token', res.token); })
    );
  }

  sendForgotPinCode(pinTempToken: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/pin/forgot/send-code`, { pinTempToken });
  }

  verifyForgotPinCode(pinTempToken: string, code: string): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.API_URL}/pin/forgot/verify-code`, { pinTempToken, code }).pipe(
      tap(res => { if (res.token) localStorage.setItem('token', res.token); })
    );
  }

  // ── PIN Settings (authenticated) ────────────────────────────────────────
  enablePin(pin: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.CLIENT_URL}/pin/enable`, { pin });
  }

  disablePin(pin: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.CLIENT_URL}/pin/disable`, { pin });
  }

  changePin(oldPin: string, newPin: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.CLIENT_URL}/pin/change`, { oldPin, newPin });
  }

  sendPinResetFromSettings(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.CLIENT_URL}/pin/forgot/send-code`, {});
  }

  verifyPinResetFromSettings(code: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.CLIENT_URL}/pin/forgot/verify-code`, { code });
  }

  // ── Profile ──────────────────────────────────────────────────────────────
  getCurrentUser(): Observable<ClientProfile> {
    return this.http.get<ClientProfile>(`${this.CLIENT_URL}/current`);
  }

  refreshCurrentUser(): Observable<ClientProfile> {
    return this.getCurrentUser().pipe(
      tap((u) => {
        const prev = this.currentUserSubject.value;
        if ((prev?.photoUrl ?? null) !== (u?.photoUrl ?? null)) this.photoCacheBuster = Date.now();
        this.currentUserSubject.next(u);
      })
    );
  }

  updateProfile(dto: UpdateClientProfileRequest): Observable<ClientProfile> {
    return this.http.put<ClientProfile>(`${this.CLIENT_URL}/current`, dto).pipe(
      tap((u) => this.currentUserSubject.next(u))
    );
  }

  changePassword(dto: ChangePasswordRequest): Observable<string> {
    return this.http.put(`${this.CLIENT_URL}/change-password`, dto, { responseType: 'text' });
  }

  uploadProfilePhoto(image: File): Observable<ClientProfile> {
    const fd = new FormData();
    fd.append('image', image);
    return this.http.put<ClientProfile>(`${this.CLIENT_URL}/photo`, fd).pipe(
      tap((u) => {
        this.photoCacheBuster = Date.now();
        this.currentUserSubject.next(u);
      })
    );
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

  /** Useful when `photoUrl` is behind auth and `<img>` cannot send Authorization headers. */
  fetchMediaBlob(url: string): Observable<Blob> {
    const full = url.startsWith('http') ? url : `http://localhost:8080${url}`;
    return this.http.get(full, { responseType: 'blob' });
  }
}
