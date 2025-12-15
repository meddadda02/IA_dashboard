import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';
  loading = false;

  constructor(private api: ApiService, private router: Router) { }

  onSubmit() {
    this.loading = true;
    this.error = '';

    this.api.login({ username: this.username, password: this.password }).subscribe({
      next: (response) => {
        // Check role and redirect accordingly
        // We need to decode the token or fetch the profile to know the role immediately
        // For now, let's fetch the profile to be sure
        this.api.getProfile().subscribe({
          next: (user) => {
            if (user.role === 'ROLE_ADMIN') {
              this.router.navigate(['/admin']);
            } else {
              this.router.navigate(['/dashboard']);
            }
          },
          error: () => {
            // Fallback if profile fetch fails (shouldn't happen if login succeeded)
            this.router.navigate(['/dashboard']);
          }
        });
      },
      error: (err) => {
        this.error = 'Login failed. Please check your credentials.';
        this.loading = false;
        console.error(err);
      }
    });
  }
}
