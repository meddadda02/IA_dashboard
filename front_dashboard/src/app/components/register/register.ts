import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ApiService } from '../../services/api';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule],
    templateUrl: './register.html',
    styleUrls: ['./register.css']
})
export class RegisterComponent {
    username = '';
    password = '';
    error = '';
    successMessage = '';
    loading = false;

    constructor(private api: ApiService, private router: Router) { }

    onSubmit() {
        this.loading = true;
        this.error = '';
        this.successMessage = '';

        if (!this.username || !this.password) {
            this.error = 'Veuillez remplir tous les champs';
            this.loading = false;
            return;
        }

        this.api.register({ username: this.username, password: this.password }).subscribe({
            next: () => {
                this.loading = false;
                this.successMessage = 'Compte créé avec succès ! Redirection vers la connexion...';
                setTimeout(() => {
                    this.router.navigate(['/login']);
                }, 2000);
            },
            error: (err) => {
                this.loading = false;
                if (err.status === 400 && err.error) {
                    this.error = err.error; // "Username already exists"
                } else {
                    this.error = "Erreur lors de l'inscription. Veuillez réessayer.";
                }
                console.error(err);
            }
        });
    }
}
