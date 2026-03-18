import { Routes } from '@angular/router';
import { HomeView } from './Components/Views/home-view/home-view';
import { AuthView } from './Components/Views/auth-view/auth-view';
import { AdminDashboard } from './Components/adminComponents/admin-dashboard/admin-dashboard';
import { UserManagement } from './Components/adminComponents/user-management/user-management';
import { LandingView } from './Components/Views/landing-view/landing-view';
import { adminGuard } from './Security/Guards/AdminGuard';
import { authGuard } from './Security/Guards/AuthGuard';
import { loginGuard } from './Security/Guards/LoginGuard';
import { SupportView } from './Components/Views/support-view/support-view';
import { VirementView } from './Components/Views/virement-view/virement-view';
import { AdminSupport } from './Components/adminComponents/admin-support/admin-support';
import { ActualiteView } from './Components/Views/actualite-view/actualite-view';
import { ActualiteManagement } from './Components/adminComponents/actualite-management/actualite-management';
import { CompteView } from './Components/Views/compte-view/compte-view';
import { CompteManagement } from './Components/adminComponents/compte-management/compte-management';
import { AccountDetailView } from './Components/Views/account-detail-view/account-detail-view';
import { ReceiveView } from './Components/Views/receive-view/receive-view';
import { TreasuryComponent } from './Components/adminComponents/treasury-component/treasury-component';
import { PricingView } from './Components/Views/pricing-view/pricing-view';
import { SettingsView } from './Components/Views/settings-view/settings-view';
import { NotificationsView } from './Components/Views/notifications-view/notifications-view';

export const routes: Routes = [
  {
    path: 'home-view',
    component: HomeView,
    canActivate: [authGuard],
  },
  {
    path: 'account/:id',
    component: AccountDetailView,
    canActivate: [authGuard],
  },
  {
    path: 'receive',
    component: ReceiveView,
    canActivate: [authGuard]
  },
  {
    path: 'auth-view',
    component: AuthView,
    canActivate: [loginGuard],
  },
  {
    path: 'admin-dashboard',
    component: AdminDashboard,
    canActivate: [authGuard, adminGuard],
  },
  {
    path: 'treasury-component',
    component: TreasuryComponent,
    canActivate: [authGuard, adminGuard],
  },
  {
    path: 'landing-view',
    component: LandingView,
  },
  {
    path: 'pricing-view',
    component: PricingView,
    canActivate: [authGuard],
  },
  {
    path: 'settings-view',
    component: SettingsView,
    canActivate: [authGuard],
  },
  {
    path: 'notifications-view',
    component: NotificationsView,
    canActivate: [authGuard],
  },
  {
    path: 'support-view',
    component: SupportView,
    canActivate: [authGuard],
  },
  {
    path: 'virement-view',
    component: VirementView,
    canActivate: [authGuard],
  },
  {
    path: 'actualite-view',
    component: ActualiteView,
    canActivate: [authGuard],
  },
  {
    path: 'compte-view',
    component: CompteView,
    canActivate: [authGuard],
  },
  {
    path: '',
    redirectTo: 'landing-view',
    pathMatch: 'full',
  },
  {
    path: '**',
    redirectTo: 'landing-view',
  },
];
