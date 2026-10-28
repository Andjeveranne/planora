import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AttendanceService, CourseCatalogItem } from '../services/attendance.service';

@Component({
  selector: 'app-student-course-enrollment',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="catalog-shell">
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
          <a routerLink="/student-course-enrollment" class="nav-link active">
            <span>◫</span>
            <span>Catalogue UE</span>
          </a>
          <a routerLink="/student-attendance-qr" class="nav-link">
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

      <main class="catalog-main">
        <div class="page-header">
          <div>
            <p class="eyebrow">Specialty</p>
            <h2>UE catalog</h2>
          </div>
          <a routerLink="/student-dashboard" class="primary-btn">Back to dashboard</a>
        </div>

        <div class="stats-strip">
          <div class="stat-card">
            <span>Available courses</span>
            <strong>{{ availableCourses.length }}</strong>
          </div>
          <div class="stat-card accent-green">
            <span>Enrolled</span>
            <strong>{{ enrolledCount }}</strong>
          </div>
        </div>

        <div class="filter-row">
          <input type="text" [(ngModel)]="searchTerm" placeholder="Search UE, code, teacher..." />
          <select [(ngModel)]="specialtyFilter">
            <option value="all">All specialties</option>
            <option *ngFor="let value of specialtyOptions" [value]="value">{{ value }}</option>
          </select>
        </div>

        <div class="specialty-groups" *ngFor="let group of specialtyGroups | keyvalue">
          <div class="group-header">
            <h3>{{ group.key }}</h3>
            <span>{{ group.value.length }} courses</span>
          </div>

          <div class="course-grid">
            <article class="course-card" *ngFor="let course of group.value">
              <div class="card-top">
                <span class="course-code">{{ course.code }}</span>
                <span class="status-badge" [class.enrolled]="course.enrolled">
                  {{ course.enrolled ? 'Enrolled' : 'Available' }}
                </span>
              </div>

              <h4>{{ course.title }}</h4>
              <p>{{ course.teacherName }} • {{ course.type }}</p>
              <ul>
                <li>{{ course.schedule }}</li>
                <li>{{ course.room }}</li>
                <li>{{ course.credits }} credits</li>
              </ul>

              <button class="primary-btn" *ngIf="!course.enrolled" (click)="enroll(course)">Enroll</button>
              <button class="secondary-btn" *ngIf="course.enrolled" disabled>Already enrolled</button>
            </article>
          </div>
        </div>
      </main>
    </div>
  `,
  styles: [
    `
      .catalog-shell {
        display: flex;
        min-height: 100vh;
        background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
        color: #0f172a;
        overflow: hidden;
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

      .catalog-main {
        flex: 1;
        padding: 32px;
        overflow-y: auto;
      }

      .page-header, .stats-strip, .card-top, .group-header, .filter-row {
        display: flex;
        align-items: center;
        justify-content: space-between;
      }

      .page-header { margin-bottom: 24px; }
      .eyebrow {
        margin: 0 0 8px;
        text-transform: uppercase;
        letter-spacing: 0.12em;
        color: #64748b;
        font-size: 12px;
      }
      h2 { margin: 0; font-size: 2rem; }
      .primary-btn, .secondary-btn {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        border: none;
        border-radius: 12px;
        padding: 12px 16px;
        font-weight: 700;
        text-decoration: none;
        cursor: pointer;
      }
      .primary-btn { background: linear-gradient(135deg, #4f46e5, #2563eb); color: white; }
      .secondary-btn { background: #e2e8f0; color: #334155; }
      .stats-strip {
        gap: 20px;
        margin-bottom: 28px;
      }
      .stat-card {
        flex: 1;
        background: white;
        padding: 22px 20px;
        border-radius: 20px;
        box-shadow: 0 14px 30px rgba(15,23,42,0.06);
      }
      .stat-card span { color: #64748b; }
      .stat-card strong { display: block; margin-top: 10px; font-size: 2rem; }
      .accent-green { background: linear-gradient(135deg, #10b981, #34d399); color: white; }
      .accent-green span { color: rgba(255,255,255,0.8); }
      .filter-row {
        gap: 12px;
        margin-bottom: 24px;
      }
      .filter-row input, .filter-row select {
        flex: 1;
        border: 1px solid #dfeaf7;
        background: rgba(255,255,255,0.9);
        padding: 12px 14px;
        border-radius: 12px;
      }
      .specialty-groups { margin-top: 24px; }
      .group-header {
        padding: 0 6px 12px;
        border-bottom: 1px solid rgba(148,163,184,0.25);
        margin-bottom: 16px;
      }
      .group-header h3 { margin: 0; font-size: 1.4rem; }
      .group-header span { color: #64748b; }
      .course-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(270px, 1fr));
        gap: 20px;
      }
      .course-card {
        background: rgba(255,255,255,0.95);
        border: 1px solid rgba(148,163,184,0.2);
        border-radius: 22px;
        padding: 20px;
        box-shadow: 0 12px 28px rgba(15,23,42,0.05);
      }
      .course-code {
        display: inline-flex;
        border-radius: 999px;
        background: rgba(79,70,229,0.12);
        color: #4338ca;
        padding: 6px 10px;
        font-weight: 700;
        font-size: 12px;
      }
      .status-badge {
        padding: 5px 10px;
        border-radius: 999px;
        background: #e0f2fe;
        color: #0369a1;
        font-size: 12px;
        font-weight: 700;
      }
      .status-badge.enrolled {
        background: #dcfce7;
        color: #166534;
      }
      h4 { margin: 16px 0 8px; font-size: 1.2rem; }
      p { margin: 0 0 12px; color: #475569; }
      ul { margin: 0 0 18px; padding-left: 18px; color: #334155; }
      li + li { margin-top: 4px; }
      @media (max-width: 960px) {
        .catalog-shell { display: block; }
        .student-sidebar { width: 100%; height: auto; position: static; }
        .catalog-main { padding: 20px; }
      }
      @media (max-width: 768px) {
        .page-header, .stats-strip, .group-header, .filter-row { flex-direction: column; align-items: flex-start; }
      }
    `
  ]
})
export class StudentCourseEnrollmentComponent implements OnInit {
  studentId = 'STU-1001';
  specialtyGroups: Record<string, CourseCatalogItem[]> = {};
  enrolledCount = 0;
  searchTerm = '';
  specialtyFilter = 'all';

  constructor(private attendanceService: AttendanceService) {}

  get specialtyOptions(): string[] {
    return Array.from(new Set(this.attendanceService.getCourseCatalog().map((course) => course.specialty)));
  }

  get availableCourses(): CourseCatalogItem[] {
    const enrolledIds = this.attendanceService.getStudentCourses(this.studentId).filter((course) => course.enrolled).map((course) => course.id);
    return this.attendanceService.getCourseCatalog().filter((course) => !enrolledIds.includes(course.id));
  }

  get specialtyCount(): number {
    return Object.keys(this.specialtyGroups ?? {}).length;
  }

  ngOnInit(): void {
    this.loadCourses();
  }

  enroll(course: CourseCatalogItem): void {
    const success = this.attendanceService.enrollInCourse(this.studentId, course.id);
    if (success) {
      this.loadCourses();
    }
  }

  private loadCourses(): void {
    const filtered = this.availableCourses.filter((course) => {
      const search = this.searchTerm.trim().toLowerCase();
      const matchesText = !search || [course.title, course.code, course.teacherName].join(' ').toLowerCase().includes(search);
      const matchesSpecialty = this.specialtyFilter === 'all' || course.specialty === this.specialtyFilter;
      return matchesText && matchesSpecialty;
    });

    this.specialtyGroups = filtered.reduce<Record<string, CourseCatalogItem[]>>((groups, course) => {
      groups[course.specialty] = [...(groups[course.specialty] ?? []), course];
      return groups;
    }, {});
    this.enrolledCount = this.attendanceService.getStudentCourses(this.studentId).filter((course) => course.enrolled).length;
  }
}
