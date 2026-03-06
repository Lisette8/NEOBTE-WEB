import { Routes } from '@angular/router';
import { HomeView } from './Components/Views/home-view/home-view';
import { AuthView } from './Components/Views/auth-view/auth-view';
import { Footer } from './Components/footer/footer';
import { Header } from './Components/header/header';
import { AdminDashboard } from './Components/Views/admin-dashboard/admin-dashboard';
import { UserManagement } from './Components/Views/user-management/user-management';
import { LandingView } from './Components/Views/landing-view/landing-view';
import { adminGuard } from './Security/Guards/AdminGuard';
import { authGuard } from './Security/Guards/AuthGuard';
import { loginGuard } from './Security/Guards/LoginGuard';
import { SupportView } from './Components/Views/support-view/support-view';
import { VirementView } from './Components/Views/virement-view/virement-view';
import { AdminSupport } from './Components/Views/admin-support/admin-support';

export const routes: Routes = [
    {
        path: 'home-view',
        component: HomeView,
        canActivate: [authGuard],
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
        path: 'user-management',
        component: UserManagement,
        canActivate: [authGuard, adminGuard],
    },
    {
        path: 'landing-view',
        component: LandingView,
    },
    {
        path: 'support-view',
        component: SupportView,
    },
    {
        path: 'virement-view',
        component: VirementView,
    },
    {
        path: 'admin-support',
        component: AdminSupport,
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
