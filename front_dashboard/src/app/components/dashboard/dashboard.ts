import { Component, ChangeDetectorRef } from '@angular/core';
// Force rebuild
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api';
import { AnalysisComponent } from '../analysis/analysis';
import { finalize, timeout } from 'rxjs';

import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, AnalysisComponent, RouterModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class DashboardComponent {
  selectedFile: File | null = null;
  loading = false;
  error = '';
  analysisResult: any = null;
  lastFileId: number | null = null;
  isAdmin = false;

  constructor(private api: ApiService, private cdr: ChangeDetectorRef) {
    this.checkAdminStatus();
  }

  checkAdminStatus() {
    this.api.getProfile().subscribe({
      next: (user) => {
        this.isAdmin = user.role === 'ROLE_ADMIN';
      },
      error: () => this.isAdmin = false
    });
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  onUpload() {
    if (!this.selectedFile) return;

    this.loading = true;
    this.error = '';
    this.analysisResult = null;
    this.lastFileId = null;

    this.api.uploadFile(this.selectedFile)
      .pipe(
        timeout(60000), // Timeout after 60 seconds
        finalize(() => {
          console.log('Upload request finalized');
          this.loading = false;
          this.cdr.detectChanges();
        }))
      .subscribe({
        next: (response: any) => {
          console.log('Upload response:', response);

          if (response.fileId) {
            this.lastFileId = response.fileId;
          }

          if (response.analysis) {
            this.handleAnalysisData(response.analysis);
          } else {
            console.warn('No analysis found in initial response, trying to fetch by ID...');
            if (this.lastFileId) {
              this.fetchAnalysis(this.lastFileId);
            } else {
              this.error = 'Analysis completed but no results returned and no File ID found.';
            }
          }
        },
        error: (err) => {
          this.loading = false;
          this.error = 'Upload failed: ' + (err.error?.error || err.message);
          console.error(err);
          this.cdr.detectChanges();
        }
      });
  }

  fetchAnalysis(fileId: number) {
    this.loading = true;
    this.api.getFile(fileId).subscribe({
      next: (response: any) => {
        this.loading = false;
        if (response.analysis) {
          this.handleAnalysisData(response.analysis);
        } else {
          this.error = 'File retrieved but analysis is still missing.';
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.error = 'Failed to fetch analysis: ' + err.message;
        this.cdr.detectChanges();
      }
    });
  }

  handleAnalysisData(analysisData: any) {
    console.log('Handling analysis data:', analysisData);
    if (typeof analysisData === 'string') {
      if (analysisData.startsWith('Error:')) {
        this.error = analysisData;
        this.analysisResult = null;
      } else {
        try {
          this.analysisResult = JSON.parse(analysisData);
        } catch (e) {
          console.error('Failed to parse analysis JSON string', e);
          this.analysisResult = analysisData; // Keep as string if parse fails (unlikely to work in charts but shows something)
        }
      }
    } else {
      this.analysisResult = analysisData;
    }
    this.cdr.detectChanges();
  }

  logout() {
    this.api.logout();
    window.location.reload();
  }
}
