import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Page } from '../Entities/Interfaces/page';
import { ClientNotification } from '../Entities/Interfaces/notification';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiClient = 'http://localhost:8080/api/v1/client/notifications';

  constructor(private http: HttpClient) { }

  getMyNotifications(page = 0, size = 20, unreadOnly = false): Observable<Page<ClientNotification>> {
    return this.http.get<Page<ClientNotification>>(
      `${this.apiClient}?page=${page}&size=${size}&unreadOnly=${unreadOnly}`
    );
  }

  getUnreadCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiClient}/unread-count`);
  }

  markRead(id: number): Observable<ClientNotification> {
    return this.http.post<ClientNotification>(`${this.apiClient}/${id}/read`, {});
  }

  markAllRead(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiClient}/read-all`, {});
  }
}

