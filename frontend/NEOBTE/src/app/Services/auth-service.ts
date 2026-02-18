import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private API_URL = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  // 🔐 LOGIN
  login(email: string, motDePasse: string): Observable<any> {
    return this.http.post(`${this.API_URL}/login`, {
      email,
      motDePasse
    }).pipe(
      tap((res: any) => {
        // save token in localStorage
        localStorage.setItem('token', res.token);
      })
    );
  }

  // 🆕 REGISTER
  register(data: any): Observable<any> {
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
