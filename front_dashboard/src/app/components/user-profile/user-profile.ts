import { Component, OnInit } from '@angular/core';
// Force rebuild
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api';
import { Router, RouterModule } from '@angular/router';

@Component({
    selector: 'app-user-profile',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule],
    templateUrl: './user-profile.html',
    styleUrl: './user-profile.component.css'
})
export class UserProfileComponent implements OnInit {
    user: any = { username: '', role: '' };
    password = '';
    loading = false;
    message = '';
    error = '';
    isEditing = false;

    get displayRole(): string {
        if (this.user.role === 'ROLE_ADMIN') {
            return 'Administrateur';
        } else if (this.user.role === 'ROLE_USER') {
            return 'Utilisateur';
        }
        return this.user.role || 'Membre';
    }

    constructor(private api: ApiService, private router: Router) { }

    ngOnInit() {
        this.loadProfile();
    }

    loadProfile() {
        this.api.getProfile().subscribe({
            next: (data) => {
                console.log('Profile data loaded:', data);
                this.user = data;
            },
            error: (err) => {
                console.error('Error loading profile:', err);
                this.error = 'Erreur lors du chargement du profil';
            }
        });
    }

    toggleEdit() {
        this.isEditing = !this.isEditing;
        this.message = '';
        this.error = '';
    }

    cancelEdit() {
        this.isEditing = false;
        this.password = '';
        this.message = '';
        this.error = '';
        this.loadProfile(); // Reset changes
    }

    updateProfile() {
        this.loading = true;
        this.message = '';
        this.error = '';

        const payload: any = { username: this.user.username };
        if (this.password) {
            payload.password = this.password;
        }

        this.api.updateProfile(payload).subscribe({
            next: (data) => {
                this.loading = false;
                this.message = 'Profil mis à jour avec succès';
                this.user = data;
                this.password = '';
                this.isEditing = false; // Switch back to view mode
            },
            error: (err) => {
                this.loading = false;
                this.error = 'Erreur lors de la mise à jour';
                console.error(err);
            }
        });
    }

    confirmDelete() {
        if (confirm('Êtes-vous sûr de vouloir supprimer votre compte ? Cette action est irréversible.')) {
            this.api.deleteProfile().subscribe({
                next: () => {
                    this.api.logout();
                    this.router.navigate(['/login']);
                },
                error: (err) => {
                    this.error = 'Erreur lors de la suppression du compte';
                    console.error(err);
                }
            });
        }
    }
}
