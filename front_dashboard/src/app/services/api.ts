import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = 'http://localhost:8080/api';
  private tokenKey = 'auth_token';

  constructor(private http: HttpClient) { }

  login(loginRequest: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/login`, loginRequest).pipe(
      tap((response: any) => {
        if (response.token) {
          this.saveToken(response.token);
        }
      })
    );
  }

  register(registerRequest: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/register`, registerRequest);
  }

  uploadFile(file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post(`${this.baseUrl}/files/upload`, formData, {
      headers: this.getHeaders(false) // Don't set Content-Type for FormData
    });
  }

  getFile(id: number): Observable<any> {
    return this.http.get(`${this.baseUrl}/files/${id}`, {
      headers: this.getHeaders(true)
    });
  }

  getProfile(): Observable<any> {
    return this.http.get(`${this.baseUrl}/user/me`, {
      headers: this.getHeaders(true)
    });
  }

  updateProfile(data: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/user/me`, data, {
      headers: this.getHeaders(true)
    });
  }

  deleteProfile(): Observable<any> {
    return this.http.delete(`${this.baseUrl}/user/me`, {
      headers: this.getHeaders(true)
    });
  }

  // Admin Endpoints
  getAllUsers(): Observable<any> {
    return this.http.get(`${this.baseUrl}/admin/users`, {
      headers: this.getHeaders(true)
    });
  }

  getSystemStats(): Observable<any> {
    return this.http.get(`${this.baseUrl}/admin/stats`, {
      headers: this.getHeaders(true)
    });
  }

  changeUserRole(userId: number, newRole: string): Observable<any> {
    return this.http.put(`${this.baseUrl}/admin/users/${userId}/role?newRole=${newRole}`, {}, {
      headers: this.getHeaders(true)
    });
  }

  deleteUser(userId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/admin/users/${userId}`, {
      headers: this.getHeaders(true)
    });
  }

  saveToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  private getHeaders(json: boolean = true): HttpHeaders {
    let headers = new HttpHeaders();
    const token = this.getToken();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    if (json) {
      headers = headers.set('Content-Type', 'application/json');
    }
    return headers;
  }
}
