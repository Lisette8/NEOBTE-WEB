import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../../Services/auth-service';


export const authGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    router.navigate(['/auth-view']);
    return false;
  }

  // Allow admins through to admin routes — only block them from client-only routes
  if (authService.getUserRole() === 'ADMIN') {
    const url = route.routeConfig?.path ?? '';
    if (url.startsWith('admin')) return true;
    router.navigate(['/admin-dashboard']);
    return false;
  }

  return true;
};
