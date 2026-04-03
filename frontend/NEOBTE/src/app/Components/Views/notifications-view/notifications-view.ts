import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { NotificationService } from '../../../Services/notification-service';
import { ClientNotification } from '../../../Entities/Interfaces/notification';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-notifications-view',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './notifications-view.html',
  styleUrl: './notifications-view.css',
})
export class NotificationsView implements OnInit, OnDestroy {
  notifications: ClientNotification[] = [];
  loading = false;
  error = '';
  page = 0;
  size = 20;
  totalPages = 0;
  unreadOnly = false;

  private pollSub?: Subscription;

  constructor(private notificationService: NotificationService) { }

  ngOnInit(): void {
    this.load();
    this.pollSub = interval(30000).subscribe(() => this.load());
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
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
      error: () => { this.error = 'Impossible de charger vos notifications.'; this.loading = false; },
    });
  }

  toggleUnreadOnly() { this.unreadOnly = !this.unreadOnly; this.page = 0; this.load(); }

  markAllRead() {
    this.notificationService.markAllRead().subscribe({
      next: () => { this.notifications = this.notifications.map((n) => ({ ...n, lu: true })); },
      error: () => { },
    });
  }

  markRead(n: ClientNotification) {
    if (n.lu) return;
    n.lu = true;
    this.notificationService.markRead(n.id).subscribe({ next: () => { }, error: () => { } });
  }

  nextPage() { if (this.page < this.totalPages - 1) { this.page++; this.load(); } }
  previousPage() { if (this.page > 0) { this.page--; this.load(); } }

  /** Returns a CSS class suffix for the icon bg color based on type */
  iconColor(type: ClientNotification['type']): string {
    switch (type) {
      case 'TRANSFER_SENT':
      case 'TRANSFER_RECEIVED':
        return 'purple';
      case 'LOAN_APPROVED':
      case 'INVESTMENT_MATURED':
      case 'LOAN_PAID_OFF':
        return 'green';
      case 'LOAN_PAYMENT_FAILED':
      case 'LOAN_PENALTY':
      case 'LOAN_DEFAULT':
      case 'LOAN_REJECTED':
        return 'red';
      case 'LOAN_REQUESTED':
      case 'INVESTMENT_CREATED':
        return 'amber';
      case 'PASSWORD_CHANGED':
        return 'navy';
      default:
        return 'blue';
    }
  }

  iconClass(type: ClientNotification['type']): string {
    switch (type) {
      case 'TRANSFER_SENT':
        return 'fa-solid fa-arrow-up-right-from-square';
      case 'TRANSFER_RECEIVED':
        return 'fa-solid fa-arrow-down';
      case 'PASSWORD_CHANGED':
        return 'fa-solid fa-key';
      case 'ACTUALITE_CREATED':
      case 'ACTUALITE_UPDATED':
        return 'fa-regular fa-newspaper';
      case 'REFERRAL_REWARD':
        return 'fa-solid fa-user-group';
      case 'INVESTMENT_CREATED':
        return 'fa-solid fa-chart-line';
      case 'INVESTMENT_MATURED':
        return 'fa-solid fa-coins';
      case 'LOAN_REQUESTED':
        return 'fa-solid fa-file-invoice-dollar';
      case 'LOAN_APPROVED':
        return 'fa-solid fa-circle-check';
      case 'LOAN_REJECTED':
        return 'fa-solid fa-circle-xmark';
      case 'LOAN_PAYMENT_FAILED':
        return 'fa-solid fa-triangle-exclamation';
      case 'LOAN_PENALTY':
        return 'fa-solid fa-bolt';
      case 'LOAN_DEFAULT':
        return 'fa-solid fa-skull-crossbones';
      case 'LOAN_PAID_OFF':
        return 'fa-solid fa-trophy';
      default:
        return 'fa-regular fa-bell';
    }
  }

  typeLabel(type: ClientNotification['type']): string {
    switch (type) {
      case 'TRANSFER_SENT':
      case 'TRANSFER_RECEIVED':
        return 'Virement';
      case 'PASSWORD_CHANGED':
        return 'Sécurité';
      case 'ACTUALITE_CREATED':
      case 'ACTUALITE_UPDATED':
        return 'Actualité';
      case 'REFERRAL_REWARD':
        return 'Parrainage';
      case 'INVESTMENT_CREATED':
      case 'INVESTMENT_MATURED':
        return 'Placement';
      case 'LOAN_REQUESTED':
      case 'LOAN_APPROVED':
      case 'LOAN_REJECTED':
      case 'LOAN_PAYMENT_FAILED':
      case 'LOAN_PENALTY':
      case 'LOAN_DEFAULT':
      case 'LOAN_PAID_OFF':
        return 'Prêt';
      default:
        return 'Notification';
    }
  }
}