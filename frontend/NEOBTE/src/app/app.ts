import { Component, signal } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { Header } from './Components/header/header';
import { Footer } from './Components/footer/footer';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs';
import { ConfirmModal } from './Components/Shared/confirm-modal/confirm-modal';

export type HeaderMode = 'landing' | 'app' | 'hidden';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, Header, Footer, CommonModule, ConfirmModal],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('NEOBTE');

  headerMode: HeaderMode = 'landing';
  showFooter = true;

  private readonly adminRoutes = [
    'admin-dashboard', 'admin-support', 'actualite-management',
    'compte-management', 'user-management', 'virement-management',
    'demande-management', 'fraude-management', 'treasury-component', 'ai-analytics'
  ];

  private readonly publicRoutes = ['landing-view', 'contact', 'auth-view', 'pricing-view'];

  constructor(private router: Router) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      const url: string = event.urlAfterRedirects || event.url;

      if (this.adminRoutes.some(r => url.includes(r))) {
        this.headerMode = 'hidden';
        this.showFooter = false;
      } else if (this.publicRoutes.some(r => url.includes(r))) {
        // Auth-view gets no header at all — it has its own full-screen layout.
        this.headerMode = url.includes('auth-view') ? 'hidden' : 'landing';
        this.showFooter = !url.includes('auth-view');
      } else {
        // Client area uses the premium sidebar/topbar shell.
        this.headerMode = 'hidden';
        this.showFooter = false;
      }
    });
  }
}