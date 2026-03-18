import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { NotificationService } from '../../../Services/notification-service';
import { ClientNotification } from '../../../Entities/Interfaces/notification';

@Component({
  selector: 'app-notifications-view',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './notifications-view.html',
  styleUrl: './notifications-view.css',
})
export class NotificationsView implements OnInit {
  notifications: ClientNotification[] = [];
  loading = false;
  error = '';
  page = 0;
  size = 20;
  totalPages = 0;
  unreadOnly = false;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.load();
  }

  load() {
    this.loading = true;
    this.error = '';
    this.notificationService.getMyNotifications(this.page, this.size, this.unreadOnly).subscribe({
      next: (res) => {
        this.notifications = res.content ?? [];
        this.totalPages = res.totalPages ?? 0;
        this.loading = false;
      },
      error: () => {
        this.error = "Impossible de charger vos notifications.";
        this.loading = false;
      },
    });
  }

  toggleUnreadOnly() {
    this.unreadOnly = !this.unreadOnly;
    this.page = 0;
    this.load();
  }

  markAllRead() {
    this.notificationService.markAllRead().subscribe({
      next: () => {
        this.notifications = this.notifications.map((n) => ({ ...n, lu: true }));
      },
      error: () => {},
    });
  }

  markRead(n: ClientNotification) {
    if (n.lu) return;
    n.lu = true;
    this.notificationService.markRead(n.id).subscribe({ next: () => {}, error: () => {} });
  }

  nextPage() {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.load();
    }
  }

  previousPage() {
    if (this.page > 0) {
      this.page--;
      this.load();
    }
  }
}

