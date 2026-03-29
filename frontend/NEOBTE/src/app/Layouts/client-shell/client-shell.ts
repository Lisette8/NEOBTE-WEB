import { CommonModule } from '@angular/common';
import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../Services/auth-service';
import { NotificationService } from '../../Services/notification-service';
import { WebsocketService } from '../../Services/SharedServices/websocket.service';
import { ClientNotification } from '../../Entities/Interfaces/notification';
import { Subscription } from 'rxjs';
import { ClientChatbotBubble } from '../../Components/client-chatbot-bubble/client-chatbot-bubble';
import { UiPreferencesService } from '../../Services/ui-preferences.service';

@Component({
  selector: 'app-client-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, ClientChatbotBubble],
  templateUrl: './client-shell.html',
  styleUrl: './client-shell.css',
})
export class ClientShell implements OnInit, OnDestroy {
  userName = '';
  photoUrl: string | null = null;
  notifPulse = false;

  notifOpen = false;
  notifications: ClientNotification[] = [];
  unreadCount = 0;
  notifLoading = false;

  private userId: number | null = null;
  private subs: Subscription[] = [];
  private rawPhotoPath: string | null = null;
  private avatarBlobUrl: string | null = null;
  private avatarTriedBlob = false;
  private notifCloseTimer: any = null;

  constructor(
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService,
    private websocketService: WebsocketService,
    private uiPrefs: UiPreferencesService
  ) {}

  ngOnInit(): void {
    this.userId = this.authService.getUserId();

    this.subs.push(
      this.authService.currentUser$.subscribe((u) => {
        if (!u) {
          this.userName = '';
          this.photoUrl = null;
          this.rawPhotoPath = null;
          this.avatarTriedBlob = false;
          if (this.avatarBlobUrl) URL.revokeObjectURL(this.avatarBlobUrl);
          this.avatarBlobUrl = null;
          return;
        }
        this.userName = u.prenom || u.username || '';
        this.rawPhotoPath = u.photoUrl ?? null;
        this.avatarTriedBlob = false;
        if (this.avatarBlobUrl) URL.revokeObjectURL(this.avatarBlobUrl);
        this.avatarBlobUrl = null;
        this.photoUrl = u.photoUrl ? this.mediaUrl(u.photoUrl) : null;
      })
    );

    if (this.authService.isLoggedIn()) this.authService.refreshCurrentUser().subscribe({ next: () => {}, error: () => {} });

    // Notifications (count + dropdown list + websocket realtime).
    this.loadNotifCounts();
    this.loadNotifications();
    if (this.userId) {
      this.websocketService.subscribeNotifications(this.userId, (msg) => this.onNotifReceived(msg as ClientNotification));
    }
  }

  ngOnDestroy(): void {
    this.subs.forEach((s) => s.unsubscribe());
    if (this.avatarBlobUrl) URL.revokeObjectURL(this.avatarBlobUrl);
  }

  get userInitials(): string {
    const name = (this.userName || 'Client').trim();
    const parts = name.split(/\s+/).filter(Boolean);
    const initials = parts
      .slice(0, 2)
      .map((p) => p[0]?.toUpperCase())
      .join('');
    return initials || 'C';
  }

  mediaUrl(url?: string | null): string {
    if (!url) return '';
    const base = url.startsWith('http') ? url : `http://localhost:8080${url}`;
    const b = this.authService.getPhotoCacheBuster();
    return `${base}${base.includes('?') ? '&' : '?'}v=${b}`;
  }

  onAvatarImgError() {
    if (this.rawPhotoPath && !this.avatarTriedBlob) {
      this.avatarTriedBlob = true;
      this.authService.fetchMediaBlob(this.rawPhotoPath).subscribe({
        next: (blob) => {
          if (this.avatarBlobUrl) URL.revokeObjectURL(this.avatarBlobUrl);
          this.avatarBlobUrl = URL.createObjectURL(blob);
          this.photoUrl = this.avatarBlobUrl;
        },
        error: () => {
          this.photoUrl = null;
        },
      });
      return;
    }
    this.photoUrl = null;
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/landing-view']),
      error: () => {
        localStorage.removeItem('token');
        this.router.navigate(['/landing-view']);
      },
    });
  }

  toggleNotif(open?: boolean) {
    this.notifOpen = typeof open === 'boolean' ? open : !this.notifOpen;
    if (this.notifOpen) {
      this.loadNotifications();
      this.loadNotifCounts();
    }
  }

  onNotifHover(open: boolean) {
    if (open) {
      if (this.notifCloseTimer) clearTimeout(this.notifCloseTimer);
      this.toggleNotif(true);
      return;
    }
    this.scheduleNotifClose();
  }

  onNotifDropdownHover(open: boolean) {
    if (open) {
      if (this.notifCloseTimer) clearTimeout(this.notifCloseTimer);
      return;
    }
    this.scheduleNotifClose();
  }

  private scheduleNotifClose() {
    if (this.notifCloseTimer) clearTimeout(this.notifCloseTimer);
    this.notifCloseTimer = setTimeout(() => (this.notifOpen = false), 220);
  }

  @HostListener('document:click', ['$event'])
  onDocClick(ev: MouseEvent) {
    const target = ev.target as HTMLElement | null;
    if (!target) return;
    if (target.closest('.neo-notif-wrap')) return;
    this.notifOpen = false;
  }

  @HostListener('document:pointerdown')
  onPointerDown() {
    this.uiPrefs.unlockAudio();
  }

  private loadNotifCounts() {
    this.notificationService.getUnreadCount().subscribe({
      next: (r) => (this.unreadCount = r.count ?? 0),
      error: () => {},
    });
  }

  private loadNotifications() {
    this.notifLoading = true;
    this.notificationService.getMyNotifications(0, 3, false).subscribe({
      next: (res) => {
        this.notifications = (res.content ?? []).slice(0, 3);
        this.notifLoading = false;
      },
      error: () => {
        this.notifications = [];
        this.notifLoading = false;
      },
    });
  }

  markRead(n: ClientNotification) {
    if (n.lu) return;
    n.lu = true;
    this.unreadCount = Math.max(0, this.unreadCount - 1);
    this.notificationService.markRead(n.id).subscribe({ next: () => {}, error: () => {} });
  }

  markAllRead() {
    this.notificationService.markAllRead().subscribe({
      next: () => {
        this.notifications = this.notifications.map((n) => ({ ...n, lu: true }));
        this.unreadCount = 0;
      },
      error: () => {},
    });
  }

  private onNotifReceived(n: ClientNotification) {
    // Prepend and cap list.
    this.notifications = [n, ...this.notifications].slice(0, 3);
    if (!n.lu) this.unreadCount += 1;
    this.notifPulse = true;
    setTimeout(() => (this.notifPulse = false), 900);
    this.uiPrefs.playNotificationSound();
  }
}
