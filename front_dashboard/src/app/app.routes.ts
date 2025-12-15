import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { DashboardComponent } from './components/dashboard/dashboard';
import { AuthGuard } from './guards/auth-guard';

import { UserProfileComponent } from './components/user-profile/user-profile';
import { AdminUsersComponent } from './components/admin-users/admin-users.component';

import { RegisterComponent } from './components/register/register';

export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
    { path: 'profile', component: UserProfileComponent, canActivate: [AuthGuard] },
    { path: 'admin', component: AdminUsersComponent, canActivate: [AuthGuard] }, // Should ideally have an AdminGuard
    { path: '', redirectTo: '/login', pathMatch: 'full' },
    { path: '**', redirectTo: '/login' }
];
