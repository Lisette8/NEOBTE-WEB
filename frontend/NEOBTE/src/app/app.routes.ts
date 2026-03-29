import { Routes } from '@angular/router';
import { HomeView } from './Components/Views/home-view/home-view';
import { AuthView } from './Components/Views/auth-view/auth-view';
import { AdminDashboard } from './Components/adminComponents/admin-dashboard/admin-dashboard';
import { UserManagement } from './Components/adminComponents/user-management/user-management';
import { LandingView } from './Components/Views/landing-view/landing-view';
import { ContactView } from './Components/Views/contact-view/contact-view';
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
import { PricingInappView } from './Components/Views/pricing-inapp-view/pricing-inapp-view';
import { SettingsView } from './Components/Views/settings-view/settings-view';
import { NotificationsView } from './Components/Views/notifications-view/notifications-view';
import { InvestmentView } from './Components/Views/investment-view/investment-view';
import { LoanView } from './Components/Views/loan-view/loan-view';
import { ClientShell } from './Layouts/client-shell/client-shell';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'landing-view',
    pathMatch: 'full',
  },
  {
    path: '',
    component: ClientShell,
    canActivate: [authGuard],
    children: [
      { path: 'home-view', component: HomeView },
      { path: 'compte-view', component: CompteView },
      { path: 'account/:id', component: AccountDetailView },
      { path: 'receive', component: ReceiveView },
      { path: 'virement-view', component: VirementView },
      { path: 'investment-view', component: InvestmentView },
      { path: 'loan-view', component: LoanView },
      { path: 'support-view', component: SupportView },
      { path: 'notifications-view', component: NotificationsView },
      { path: 'settings-view', component: SettingsView },
      { path: 'pricing', component: PricingInappView },
      // In-app news view (now consistent with the client chrome)
      { path: 'actualite-view', component: ActualiteView },
    ],
  },
  {
    path: 'auth-view',
    component: AuthView,
    canActivate: [loginGuard],
  },
  {
    path: 'admin-dashboard',
    component: AdminDashboard,
    canActivate: [adminGuard],
  },
  {
    path: 'treasury-component',
    component: TreasuryComponent,
    canActivate: [adminGuard],
  },
  {
    path: 'landing-view',
    component: LandingView,
  },
  {
    path: 'contact',
    component: ContactView,
  },
  {
    path: 'pricing-view',
    component: PricingView,
  },
  {
    path: '**',
    redirectTo: 'landing-view',
  },
];
