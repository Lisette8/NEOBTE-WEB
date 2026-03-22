import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../Services/auth-service';
import { NotificationService } from '../../Services/notification-service';
import { WebsocketService } from '../../Services/SharedServices/websocket.service';
import { ClientNotification } from '../../Entities/Interfaces/notification';
import { interval, Subscription } from 'rxjs';
import { HeaderMode } from '../../app';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header implements OnInit, OnDestroy {
  @Input() mode: HeaderMode = 'app';

  notifOpen = false;
  servicesOpen = false;
  unreadCount = 0;
  latest: ClientNotification[] = [];

  private routerSub?: Subscription;
  private pollSub?: Subscription;
  private notificationsInitialized = false;

  // BTE services for the landing dropdown
  readonly bteServices = [
    { label: 'Compte Chèque', desc: 'Gestion des opérations quotidiennes', url: 'https://www.bte.com.tn/fr/particuliers/compte-cheque' },
    { label: 'Carte Bancaire', desc: 'Visa & Mastercard, paiement partout', url: 'https://www.bte.com.tn/fr/particuliers/carte-bancaire' },
    { label: 'Plan d\'Épargne', desc: 'Faites fructifier votre argent', url: 'https://www.bte.com.tn/fr/particuliers/compte-et-plan-d-epargne' },
    { label: 'Crédit', desc: 'Prêts personnels & immobiliers', url: 'https://www.bte.com.tn/fr/particuliers/besoin-d-un-credit' },
    { label: 'BTE Green', desc: 'Finance durable & crédits verts', url: 'https://www.bte.com.tn/fr/bte-green' },
    { label: 'BTE Leasing', desc: 'Financement de matériel & véhicules', url: 'https://www.bte.com.tn/fr/bte-services/bte-leasing' },
    { label: 'Services Internationaux', desc: 'Transferts & opérations en devises', url: 'https://www.bte.com.tn/fr/particuliers/services-internationaux' },
    { label: 'BTE Packs', desc: 'Offres groupées pour professionnels', url: 'https://www.bte.com.tn/fr/bte-packs' },
  ];

  constructor(
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService,
  ) { }

  ngOnInit(): void {
    this.routerSub = this.router.events.subscribe(() => {
      this.servicesOpen = false;
      this.notifOpen = false;
      const logged = this.isLoggedIn();
      if (logged && !this.notificationsInitialized && this.mode === 'app') this.initNotifications();
      if (!logged && this.notificationsInitialized) this.resetNotifications();
    });
    if (this.isLoggedIn() && this.mode === 'app') this.initNotifications();
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
    this.pollSub?.unsubscribe();
  }

  isLoggedIn(): boolean { return this.authService.isLoggedIn(); }
  toggleNotifications() { this.notifOpen = !this.notifOpen; }
  closeNotifications() { this.notifOpen = false; }

  openNotification(n: ClientNotification) {
    if (!n.lu) {
      n.lu = true;
      this.unreadCount = Math.max(0, this.unreadCount - 1);
      this.notificationService.markRead(n.id).subscribe({ error: () => { } });
    }
    this.closeNotifications();
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
      next: () => { this.resetNotifications(); this.router.navigate(['/landing-view']); },
      error: () => { localStorage.removeItem('token'); this.resetNotifications(); this.router.navigate(['/landing-view']); }
    });
  }

  private initNotifications() {
    if (!this.authService.getUserId()) return;
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