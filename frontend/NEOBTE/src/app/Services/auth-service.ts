import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { RegisterRequest } from '../Entities/Interfaces/register-request';
import { AuthResponse } from '../Entities/Interfaces/auth-response';
import { LoginRequest } from '../Entities/Interfaces/login-request';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private API_URL = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  // 🔐 LOGIN
  login(data: LoginRequest) {
  return this.http.post<AuthResponse>(`${this.API_URL}/login`, data)
    .pipe(
      tap(res => {
        localStorage.setItem('token', res.token);
      })
    );
}

  // 🆕 REGISTER
  register(data: RegisterRequest) {
    return this.http.post(`${this.API_URL}/register`, data);
  }

  // 🚪 LOGOUT
  logout(): void {
    localStorage.removeItem('token');
  }

  // 📦 GET TOKEN
  getToken(): string | null {
    return localStorage.getItem('token');
  }

  // 🔎 CHECK LOGIN
  isLoggedIn(): boolean {
    return this.getToken() != null;
  }
}
