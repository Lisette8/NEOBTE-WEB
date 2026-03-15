import { Component, signal } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { Header } from './Components/header/header';
import { Footer } from './Components/footer/footer';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs';
import { ConfirmModal } from './Components/Shared/confirm-modal/confirm-modal';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, Header, Footer, CommonModule, ConfirmModal],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('NEOBTE');
  showLayout = true;
 
  // Routes that should NOT show the header/footer (admin dashboard has its own chrome)
  private readonly noLayoutRoutes = ['admin-dashboard', 'admin-support', 'actualite-management', 'compte-management', 'user-management', 'virement-management'];
 
  constructor(private router: Router) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      const url: string = event.urlAfterRedirects || event.url;
      // Hide header/footer only for admin routes — show for all client-facing routes
      this.showLayout = !this.noLayoutRoutes.some(route => url.includes(route));
    });
  }
}