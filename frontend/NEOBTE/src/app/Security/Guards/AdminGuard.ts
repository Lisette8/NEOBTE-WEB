import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../../Services/auth-service';


export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const role = authService.getUserRole();

  if (role !== 'ADMIN') {
    router.navigate(['/auth-view']);
    return false;
  }

  return true;
};
