import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../Services/auth-service';
import { NotificationService } from '../../Services/notification-service';
import { WebsocketService } from '../../Services/SharedServices/websocket.service';
import { ClientNotification } from '../../Entities/Interfaces/notification';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header implements OnInit, OnDestroy {
  @Input() mode: 'landing' | 'app' | 'hidden' = 'app';

  notifOpen = false;
  unreadCount = 0;
  latest: ClientNotification[] = [];

  private pollSub?: Subscription;
  private notificationsInitialized = false;

  readonly bteServices = [
    { label: 'Particuliers', href: 'https://www.bte.com.tn/fr/particuliers' },
    { label: 'Entreprises', href: 'https://www.bte.com.tn/fr/entreprises' },
    { label: 'Crédits', href: 'https://www.bte.com.tn/fr/credits' },
    { label: 'Épargne', href: 'https://www.bte.com.tn/fr/epargne' },
    { label: 'Assurances', href: 'https://www.bte.com.tn/fr/assurances' },
    { label: 'Cartes bancaires', href: 'https://www.bte.com.tn/fr/cartes' },
  ];

  constructor(
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService,
  ) { }

  ngOnInit(): void {
    if (this.isClientLoggedIn()) this.initNotifications();
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  isLoggedIn(): boolean { return this.authService.isLoggedIn(); }
  isClientLoggedIn(): boolean { return this.isLoggedIn() && this.authService.getUserRole() === 'CLIENT'; }
  isAdminLoggedIn(): boolean { return this.isLoggedIn() && this.authService.getUserRole() === 'ADMIN'; }

  toggleNotifications() { this.notifOpen = !this.notifOpen; }
  closeAll() { this.notifOpen = false; }

  openNotification(n: ClientNotification) {
    if (!n.lu) {
      n.lu = true;
      this.unreadCount = Math.max(0, this.unreadCount - 1);
      this.notificationService.markRead(n.id).subscribe({ error: () => { } });
    }
    this.closeAll();
    if (n.lien) this.router.navigate([n.lien]);
  }

  markAllRead() {
    this.notificationService.markAllRead().subscribe({
      next: () => { this.unreadCount = 0; this.latest = this.latest.map(n => ({ ...n, lu: true })); },
      error: () => { },
    });
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => { this.resetNotifications(); this.router.navigate(['/auth-view']); },
      error: () => { localStorage.removeItem('token'); this.resetNotifications(); this.router.navigate(['/auth-view']); }
    });
  }

  private initNotifications() {
    if (this.notificationsInitialized) return;
    this.notificationsInitialized = true;
    this.fetchNotifications();
    this.pollSub = interval(2000).subscribe(() => this.fetchNotifications());
  }

  private fetchNotifications() {
    this.notificationService.getUnreadCount().subscribe({
      next: (res) => this.unreadCount = Number(res.count ?? 0),
      error: () => { },
    });
    this.notificationService.getMyNotifications(0, 5, false).subscribe({
      next: (page) => this.latest = page.content ?? [],
      error: () => { },
    });
  }

  private resetNotifications() {
    this.notificationsInitialized = false;
    this.notifOpen = false;
    this.unreadCount = 0;
    this.latest = [];
    this.pollSub?.unsubscribe();
    this.pollSub = undefined;
  }
}