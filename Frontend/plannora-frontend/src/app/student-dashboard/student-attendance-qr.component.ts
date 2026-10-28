import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AttendanceService, CourseCatalogItem } from '../services/attendance.service';

@Component({
  selector: 'app-student-attendance-qr',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="attendance-shell">
      <aside class="student-sidebar">
        <div class="brand-block">
          <div class="brand-mark">P</div>
          <div>
            <h2>PLANORA</h2>
            <small>Portail étudiant</small>
          </div>
        </div>

        <nav class="student-nav">
          <a routerLink="/student-dashboard" class="nav-link">
            <span>⌂</span>
            <span>Vue d'ensemble</span>
          </a>
          <a routerLink="/student-course-enrollment" class="nav-link">
            <span>◫</span>
            <span>Catalogue UE</span>
          </a>
          <a routerLink="/student-attendance-qr" class="nav-link active">
            <span>◌</span>
            <span>Scanner le QR</span>
          </a>
        </nav>

        <div class="sidebar-footer">
          <div class="mini-profile">
            <div class="avatar">ST</div>
            <div>
              <strong>Étudiant Planora</strong>
              <small>Software Engineering</small>
            </div>
          </div>
        </div>
      </aside>

      <main class="attendance-qr-page">
        <div class="page-header">
          <div>
            <p class="eyebrow">Présence</p>
            <h2>Scanner le QR de présence</h2>
          </div>
          <button class="primary-btn" (click)="refreshCourses()">Actualiser</button>
        </div>

        <div class="summary-grid">
          <div class="summary-card primary">
            <span>Cours de la spécialité</span>
            <strong>{{ enrolledCourses.length }}</strong>
          </div>
          <div class="summary-card success">
            <span>Présences validées</span>
            <strong>{{ validatedCount }}</strong>
          </div>
        </div>

        <div class="course-list-panel" *ngIf="enrolledCourses.length; else emptyState">
          <div class="course-row" *ngFor="let course of enrolledCourses">
            <div>
              <div class="course-code">{{ course.code }}</div>
              <h3>{{ course.title }}</h3>
              <small>{{ course.schedule }} • {{ course.room }}</small>
            </div>

            <div class="course-actions">
              <button class="secondary-btn" [disabled]="!activeSession || activeSession.courseId !== course.id" (click)="openCameraFor(course)">
                {{ activeSession && activeSession.courseId === course.id ? 'Scanner maintenant' : 'Pas de QR actif' }}
              </button>
            </div>
          </div>
        </div>

        <ng-template #emptyState>
          <div class="empty-state">
            <h3>Aucun cours inscrit</h3>
            <p>Inscrivez-vous à une UE pour accéder au scan de présence.</p>
          </div>
        </ng-template>

        <div class="scanner-panel" *ngIf="isCameraOpen">
          <div class="scanner-header">
            <h3>Caméra de scan</h3>
            <button class="ghost-btn" (click)="closeCamera()">Fermer</button>
          </div>

          <video #video autoplay playsinline muted class="scanner-video"></video>
          <canvas #canvas class="scanner-canvas" hidden></canvas>

          <div class="scan-status" *ngIf="scanMessage">
            {{ scanMessage }}
          </div>

          <button class="primary-btn full-width" (click)="captureAndScan()">Valider la présence</button>
        </div>
      </main>
    </div>
  `,
  styles: [
    `
      .attendance-shell {
        display: flex;
        min-height: 100vh;
        background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
        color: #0f172a;
      }

      .student-sidebar {
        width: 280px;
        background: linear-gradient(180deg, #0f172a 0%, #111827 100%);
        color: #edf2ff;
        padding: 26px 18px;
        position: sticky;
        top: 0;
        height: 100vh;
        display: flex;
        flex-direction: column;
        border-right: 1px solid rgba(148, 163, 184, 0.15);
        box-shadow: 16px 0 40px rgba(15, 23, 42, 0.2);
      }

      .brand-block {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 28px;
        padding: 8px 10px;
      }

      .brand-mark {
        width: 42px;
        height: 42px;
        border-radius: 14px;
        background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
        display: grid;
        place-items: center;
        font-weight: 800;
      }

      .brand-block h2 {
        margin: 0;
        font-size: 1.35rem;
        letter-spacing: 0.08em;
      }

      .brand-block small {
        color: #a5b4fc;
      }

      .student-nav {
        display: flex;
        flex-direction: column;
        gap: 8px;
      }

      .nav-link {
        text-decoration: none;
        color: #dbeafe;
        display: flex;
        align-items: center;
        gap: 12px;
        width: 100%;
        border-radius: 14px;
        padding: 12px 14px;
        transition: all 0.2s ease;
      }

      .nav-link:hover,
      .nav-link.active {
        background: rgba(99, 102, 241, 0.18);
        color: #ffffff;
        box-shadow: inset 0 0 0 1px rgba(129, 140, 248, 0.22);
      }

      .sidebar-footer {
        margin-top: auto;
        padding-top: 18px;
        border-top: 1px solid rgba(148, 163, 184, 0.15);
      }

      .mini-profile {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 10px;
        border-radius: 14px;
        background: rgba(15, 23, 42, 0.35);
      }

      .avatar {
        width: 42px;
        height: 42px;
        border-radius: 50%;
        display: grid;
        place-items: center;
        background: linear-gradient(135deg, #4f46e5, #22c55e);
        color: white;
        font-size: 0.8rem;
        font-weight: 700;
      }

      .attendance-qr-page {
        flex: 1;
        padding: 32px;
        background: linear-gradient(135deg, #eef4ff 0%, #f8fafc 100%);
        min-height: 100vh;
        color: #0f172a;
      }
      .page-header, .summary-grid, .course-row, .scanner-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
      .page-header {
        margin-bottom: 24px;
      }
      .eyebrow {
        margin: 0 0 8px;
        font-size: 12px;
        letter-spacing: 0.12em;
        text-transform: uppercase;
        color: #64748b;
      }
      h2 { margin: 0; font-size: 2rem; }
      .summary-grid {
        gap: 18px;
        margin-bottom: 24px;
      }
      .summary-card {
        flex: 1;
        padding: 22px;
        border-radius: 20px;
        color: white;
        display: flex;
        flex-direction: column;
        gap: 10px;
        box-shadow: 0 18px 35px rgba(15,23,42,0.08);
      }
      .summary-card.primary { background: linear-gradient(135deg, #4f46e5, #3b82f6); }
      .summary-card.success { background: linear-gradient(135deg, #10b981, #22c55e); }
      .summary-card strong { font-size: 2rem; }
      .course-list-panel {
        background: rgba(255,255,255,0.9);
        border: 1px solid rgba(148,163,184,0.2);
        border-radius: 22px;
        box-shadow: 0 12px 28px rgba(15,23,42,0.05);
        overflow: hidden;
      }
      .course-row {
        padding: 18px 20px;
        border-bottom: 1px solid #edf2f7;
      }
      .course-row:last-child { border-bottom: 0; }
      .course-code {
        display: inline-flex;
        background: rgba(79,70,229,0.12);
        color: #4338ca;
        border-radius: 999px;
        padding: 5px 10px;
        font-size: 12px;
        font-weight: 700;
      }
      h3 { margin: 8px 0 4px; font-size: 1.15rem; }
      small { color: #64748b; }
      .primary-btn, .secondary-btn, .ghost-btn {
        border: none;
        border-radius: 12px;
        padding: 12px 18px;
        font-weight: 600;
        cursor: pointer;
      }
      .primary-btn { background: linear-gradient(135deg, #4f46e5, #2563eb); color: white; }
      .secondary-btn { background: #ecfeff; color: #0f172a; }
      .ghost-btn { background: white; border: 1px solid #e2e8f0; }
      .full-width { width: 100%; margin-top: 16px; }
      .empty-state { background: rgba(255,255,255,0.9); border-radius: 18px; padding: 20px; text-align: center; }
      .scanner-panel {
        margin-top: 24px;
        background: rgba(15,23,42,0.96);
        border-radius: 22px;
        padding: 18px;
        box-shadow: 0 18px 35px rgba(15,23,42,0.18);
      }
      .scanner-video {
        display: block;
        width: 100%;
        max-width: 500px;
        margin: 18px auto;
        border-radius: 18px;
        background: #020617;
      }
      .scan-status {
        margin-top: 8px;
        color: #dbeafe;
        text-align: center;
        min-height: 24px;
      }
      @media (max-width: 960px) {
        .attendance-shell { display: block; }
        .student-sidebar { width: 100%; height: auto; position: static; }
        .attendance-qr-page { padding: 20px; }
      }
      @media (max-width: 768px) {
        .page-header, .summary-grid, .course-row, .scanner-header { flex-direction: column; align-items: flex-start; }
      }
    `
  ]
})
export class StudentAttendanceQrComponent implements OnInit {
  enrolledCourses: CourseCatalogItem[] = [];
  activeSession: any = null;
  isCameraOpen = false;
  scanMessage = '';
  validatedCount = 0;
  private stream: MediaStream | null = null;
  private studentId = 'STU-1001';

  constructor(private attendanceService: AttendanceService) {}

  ngOnInit(): void {
    this.refreshCourses();
  }

  refreshCourses(): void {
    this.enrolledCourses = this.attendanceService.getStudentCourses(this.studentId).filter((course) => course.enrolled);
    this.activeSession = this.attendanceService.getActiveSessionForCourse(this.enrolledCourses[0]?.id ?? '');
    this.validatedCount = this.attendanceService.getStudentAttendanceOverview(this.studentId).filter((item: any) => {
      const status = (item.status ?? '').trim().toLowerCase();
      return status === 'present' || status === 'présent';
    }).length;
  }

  openCameraFor(course: CourseCatalogItem): void {
    this.activeSession = this.attendanceService.getActiveSessionForCourse(course.id);
    if (!this.activeSession) {
      this.scanMessage = 'Aucune séance active pour ce cours pour le moment.';
      return;
    }

    this.isCameraOpen = true;
    this.scanMessage = 'Camera prête. Scannez le QR code du cours.';
    this.startCamera();
  }

  closeCamera(): void {
    this.isCameraOpen = false;
    if (this.stream) {
      this.stream.getTracks().forEach((track) => track.stop());
      this.stream = null;
    }
  }

  captureAndScan(): void {
    const payload = JSON.stringify({
      type: 'PLANORA_ATTENDANCE',
      studentId: this.studentId,
      sessionId: this.activeSession?.id,
      courseId: this.activeSession?.courseId
    });

    const result = this.attendanceService.scanAttendance(this.studentId, this.activeSession.id);
    this.scanMessage = result.message;
    if (result.success) {
      this.closeCamera();
      this.refreshCourses();
    }
  }

  private async startCamera(): Promise<void> {
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } });
      const video = document.querySelector('video') as HTMLVideoElement | null;
      if (video) {
        video.srcObject = this.stream;
        await video.play();
      }
    } catch (error) {
      this.scanMessage = 'Impossible d’accéder à la caméra. Vérifiez les autorisations.';
    }
  }
}
