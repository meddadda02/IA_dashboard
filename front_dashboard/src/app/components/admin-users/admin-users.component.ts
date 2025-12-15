import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../services/api';

@Component({
    selector: 'app-admin-users',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './admin-users.component.html',
    styleUrl: './admin-users.component.css'
})
export class AdminUsersComponent implements OnInit {
    users: any[] = [];
    stats: any = {};
    searchTerm: string = '';
    loading: boolean = false;

    constructor(private api: ApiService) { }

    ngOnInit() {
        this.loadUsers();
    }

    loadUsers() {
        this.loading = true;
        this.api.getAllUsers().subscribe({
            next: (data) => {
                this.users = data;
                this.loading = false;
            },
            error: (err) => {
                console.error('Error loading users', err);
                this.loading = false;
            }
        });

        this.api.getSystemStats().subscribe({
            next: (data) => this.stats = data,
            error: (err) => console.error('Error loading stats', err)
        });
    }

    filteredUsers() {
        if (!this.searchTerm) return this.users;
        return this.users.filter(user =>
            user.username.toLowerCase().includes(this.searchTerm.toLowerCase())
        );
    }

    toggleRole(user: any) {
        const newRole = user.role === 'ROLE_ADMIN' ? 'ROLE_USER' : 'ROLE_ADMIN';
        const action = newRole === 'ROLE_ADMIN' ? 'promouvoir' : 'rétrograder';

        if (confirm(`Voulez-vous vraiment ${action} ${user.username} ?`)) {
            this.api.changeUserRole(user.id, newRole).subscribe({
                next: (updatedUser) => {
                    // Update the user in the local list
                    const index = this.users.findIndex(u => u.id === user.id);
                    if (index !== -1) {
                        this.users[index] = updatedUser;
                    }
                    this.loadUsers(); // Reload stats
                },
                error: (err) => alert('Erreur lors du changement de rôle')
            });
        }
    }

    deleteUser(user: any) {
        if (confirm(`Êtes-vous sûr de vouloir supprimer l'utilisateur ${user.username} ? Cette action est irréversible.`)) {
            this.api.deleteUser(user.id).subscribe({
                next: () => {
                    this.users = this.users.filter(u => u.id !== user.id);
                    this.loadUsers(); // Reload stats
                },
                error: (err) => alert('Erreur lors de la suppression de l\'utilisateur')
            });
        }
    }

    logout() {
        this.api.logout();
        window.location.reload();
    }
}
