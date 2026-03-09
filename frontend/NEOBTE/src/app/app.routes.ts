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
        path: 'actualite-view',
        component: ActualiteView,
    },
    {
        path: 'actualite-management',
        component: ActualiteManagement,
        canActivate: [authGuard, adminGuard],
    },
    {
        path: 'compte-view',
        component: CompteView,
        canActivate: [authGuard],
    },
    {
        path: 'compte-management',
        component: CompteManagement,
        canActivate: [authGuard, adminGuard],
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
