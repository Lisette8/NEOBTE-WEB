import { Component, OnDestroy, OnInit } from '@angular/core';
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
  isMenuOpen = false;
  notifOpen = false;
  unreadCount = 0;
  latest: ClientNotification[] = [];

  private routerSub?: Subscription;
  private pollSub?: Subscription;
  private notificationsInitialized = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService,
  ) { }

  ngOnInit(): void {
    this.routerSub = this.router.events.subscribe(() => {
      const logged = this.isLoggedIn();
      if (logged && !this.notificationsInitialized) this.initNotifications();
      if (!logged && this.notificationsInitialized) this.resetNotifications();
    });
    if (this.isLoggedIn()) this.initNotifications();
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
    this.pollSub?.unsubscribe();
  }

  toggleMenu() { this.isMenuOpen = !this.isMenuOpen; }
  isLoggedIn(): boolean { return this.authService.isLoggedIn(); }
  toggleNotifications() { this.notifOpen = !this.notifOpen; }
  closeNotifications() { this.notifOpen = false; }

  openNotification(n: ClientNotification) {
    if (!n.lu) {
      n.lu = true;
      this.unreadCount = Math.max(0, this.unreadCount - 1);
      this.notificationService.markRead(n.id).subscribe({ next: () => { }, error: () => { } });
    }
    this.closeNotifications();
    if (n.lien) this.router.navigate([n.lien]);
  }

  markAllRead() {
    this.notificationService.markAllRead().subscribe({
      next: () => {
        this.unreadCount = 0;
        this.latest = this.latest.map((n) => ({ ...n, lu: true }));
      },
      error: () => { },
    });
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => { this.resetNotifications(); this.router.navigate(['/auth-view']); },
      error: () => {
        localStorage.removeItem('token');
        this.resetNotifications();
        this.router.navigate(['/auth-view']);
      }
    });
  }

  private initNotifications() {
    const userId = this.authService.getUserId();
    if (!userId) return;
    this.notificationsInitialized = true;
    this.fetchNotifications();
    // Poll every 5s for new notifications
    this.pollSub = interval(2000).subscribe(() => this.fetchNotifications());
  }

  private fetchNotifications() {
    this.notificationService.getUnreadCount().subscribe({
      next: (res) => (this.unreadCount = Number(res.count ?? 0)),
      error: () => { },
    });
    this.notificationService.getMyNotifications(0, 5, false).subscribe({
      next: (page) => (this.latest = page.content ?? []),
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