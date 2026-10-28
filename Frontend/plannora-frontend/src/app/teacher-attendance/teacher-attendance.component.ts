import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription, interval } from 'rxjs';
import {
  AttendanceRecord,
  AttendanceService,
  AttendanceSession,
  CourseCatalogItem,
  TeacherStudentRecord
} from '../services/attendance.service';

@Component({
  selector: 'app-teacher-attendance',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="teacher-shell">
      <!-- Top Navigation Header -->
      <header class="top-nav">
        <div class="brand">
          <div class="brand-logo">P</div>
          <div>
            <span class="brand-title">PLANORA</span>
            <span class="brand-badge">TEACHER PORTAL</span>
          </div>
        </div>

        <div class="center-tabs">
          <button class="nav-tab" [class.active]="currentTab === 'live'" (click)="currentTab = 'live'">
            <span>⚡ Live Sessions & QR</span>
            <span class="badge-count" *ngIf="activeSessions.length > 0">{{ activeSessions.length }}</span>
          </button>
          <button class="nav-tab" [class.active]="currentTab === 'courses'" (click)="currentTab = 'courses'">
            <span>📚 My Courses ({{ teacherCourses.length }})</span>
          </button>
          <button class="nav-tab" [class.active]="currentTab === 'roster'" (click)="currentTab = 'roster'">
            <span>👥 Roster & Attendance</span>
          </button>
          <button class="nav-tab" [class.active]="currentTab === 'reports'" (click)="currentTab = 'reports'">
            <span>📊 Reports & Analytics</span>
          </button>
        </div>

        <div class="user-strip">
          <div class="profile-meta">
            <span class="user-name">Dr. Jean Dupont</span>
            <span class="user-sub">Software Engineering • Senior Lecturer</span>
          </div>
          <div class="avatar-ring">JD</div>
        </div>
      </header>

      <!-- Main Canvas -->
      <main class="dashboard-canvas">
        <!-- Hero Header Strip -->
        <div class="hero-strip">
          <div>
            <h2>Dynamic QR Attendance & Lecture Management</h2>
            <p class="subtitle">Live classroom monitoring, rotating anti-cheat QR tokens, and real-time attendance verification.</p>
          </div>
          <div class="action-buttons">
            <button class="btn-refresh" (click)="refreshAll()">
              <span>🔄 Refresh</span>
            </button>
            <button class="btn-primary" (click)="openLaunchModal(teacherCourses[0])" *ngIf="teacherCourses.length > 0">
              <span>⚡ Start QR Session</span>
            </button>
          </div>
        </div>

        <!-- 3 Quick KPI Strips -->
        <div class="kpi-grid">
          <div class="kpi-card purple">
            <div class="kpi-head">
              <span class="label">Active Live Sessions</span>
              <span class="icon">📡</span>
            </div>
            <div class="val">{{ activeSessions.length }}</div>
            <div class="desc">
              <span class="pill-active" *ngIf="activeSessions.length > 0">Check-in Active</span>
              <span class="pill-idle" *ngIf="activeSessions.length === 0">No Live Session</span>
            </div>
          </div>

          <div class="kpi-card green">
            <div class="kpi-head">
              <span class="label">Present in Class</span>
              <span class="icon">👥</span>
            </div>
            <div class="val">{{ liveAttendees.length }} / {{ totalEnrolledStudents }}</div>
            <div class="desc">
              <span class="pill-rate">Attendance Rate: {{ currentSessionRate }}%</span>
            </div>
          </div>

          <div class="kpi-card blue">
            <div class="kpi-head">
              <span class="label">Assigned Courses</span>
              <span class="icon">📖</span>
            </div>
            <div class="val">{{ teacherCourses.length }}</div>
            <div class="desc">
              <span class="pill-info">Academic Semester S1</span>
            </div>
          </div>
        </div>

        <!-- ======================================================== -->
        <!-- TAB 1: LIVE SESSIONS & DYNAMIC QR GENERATOR             -->
        <!-- ======================================================== -->
        <section *ngIf="currentTab === 'live'" class="tab-section">
          <div class="split-live-layout" *ngIf="activeSessions.length > 0; else noLiveSessions">
            <!-- Left: Dynamic QR Stage -->
            <div class="qr-stage-card" *ngIf="selectedLiveSession as live">
              <div class="stage-header">
                <div class="live-pill">
                  <span class="pulse-dot"></span>
                  <span>LIVE ONGOING SESSION: {{ live.courseCode }}</span>
                </div>
                <div class="timer-badge">
                  ⏱️ Expires in {{ live.remainingSeconds ? (live.remainingSeconds / 60 | number:'1.0-0') : 45 }} min
                </div>
              </div>

              <h3 class="live-title">{{ live.title }}</h3>
              <p class="live-meta">📍 Room: {{ live.room || 'Hall 3' }} • Type: {{ live.sessionType || 'Lecture' }}</p>

              <!-- Dynamic QR Code Container with Lock indicator -->
              <div class="qr-display-frame" [class.locked]="isSessionLocked">
                <div class="locked-overlay" *ngIf="isSessionLocked">
                  <span class="lock-icon">🔒</span>
                  <strong>CHECK-IN LOCKED</strong>
                  <small>No new scans are accepted while locked.</small>
                </div>

                <img [src]="getQrImage(live)" alt="Dynamic Attendance QR" class="qr-img" />

                <div class="qr-token-pill">
                  <span>Dynamic Token: </span>
                  <code>{{ live.qrToken }}</code>
                </div>

                <div class="auto-refresh-notice" *ngIf="isAutoRefreshEnabled && !isSessionLocked">
                  <span class="spin-icon">⏳</span>
                  <span>Dynamic rotation active (every 30s)</span>
                </div>
              </div>

              <!-- Live Dynamic Controls -->
              <div class="qr-controls-strip">
                <label class="toggle-control">
                  <input type="checkbox" [(ngModel)]="isAutoRefreshEnabled" />
                  <span>Auto-rotate QR (Anti-cheat)</span>
                </label>

                <button
                  class="btn-toggle-lock"
                  [class.locked]="isSessionLocked"
                  (click)="isSessionLocked = !isSessionLocked"
                >
                  {{ isSessionLocked ? '🔓 Unlock QR Code' : '🔒 Lock QR Code' }}
                </button>
              </div>

              <!-- Bottom Actions -->
              <div class="qr-actions">
                <button class="btn-regen" (click)="regenerateSession(live)" [disabled]="isSessionLocked">
                  <span>🔄 Regenerate QR Token</span>
                </button>
                <button class="btn-fullscreen" (click)="openFullscreen(live)">
                  <span>🔍 Fullscreen Projector Mode</span>
                </button>
                <button class="btn-close-session" (click)="closeSession(live)">
                  <span>⏹️ End Attendance Session</span>
                </button>
              </div>
            </div>

            <!-- Right: Real-time Live Attendees Tracking Table -->
            <div class="live-roster-card" *ngIf="selectedLiveSession as live">
              <div class="roster-header">
                <div>
                  <h3>Real-time Live Attendees ({{ filteredLiveAttendees.length }})</h3>
                  <p class="sub">Search, filter, and manual attendance override</p>
                </div>
                <button class="btn-sm-outline" (click)="loadAttendees(live.id)">Refresh</button>
              </div>

              <!-- Live search & filter bar -->
              <div class="live-search-strip">
                <input
                  type="text"
                  [(ngModel)]="liveSearchTerm"
                  placeholder="Search student name or ID..."
                  class="input-live-search"
                />
                <select [(ngModel)]="liveStatusFilter" class="select-live-status">
                  <option value="all">All Statuses</option>
                  <option value="present">Present</option>
                  <option value="late">Late</option>
                  <option value="absent">Absent</option>
                </select>
              </div>

              <!-- Live table -->
              <div class="attendees-table-wrap">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Student</th>
                      <th>Status</th>
                      <th>Time</th>
                      <th>Manual Override</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr *ngFor="let att of paginatedLiveAttendees">
                      <td>
                        <strong>{{ att.studentName }}</strong>
                        <span class="muted-id">{{ att.studentId }} • {{ att.studentEmail }}</span>
                      </td>
                      <td>
                        <span class="status-tag" [ngClass]="att.status.toLowerCase()">
                          {{ att.status }}
                        </span>
                      </td>
                      <td>{{ att.scannedAt | date:'HH:mm:ss' }}</td>
                      <td>
                        <div class="override-btn-group">
                          <button
                            class="ov-btn present"
                            [class.active]="att.status.toLowerCase() === 'present'"
                            (click)="setManualStatus(att, 'PRESENT')"
                            title="Mark Present"
                          >
                            P
                          </button>
                          <button
                            class="ov-btn late"
                            [class.active]="att.status.toLowerCase() === 'late'"
                            (click)="setManualStatus(att, 'LATE')"
                            title="Mark Late"
                          >
                            L
                          </button>
                          <button
                            class="ov-btn absent"
                            [class.active]="att.status.toLowerCase() === 'absent'"
                            (click)="setManualStatus(att, 'ABSENT')"
                            title="Mark Absent"
                          >
                            A
                          </button>
                        </div>
                      </td>
                    </tr>
                    <tr *ngIf="filteredLiveAttendees.length === 0">
                      <td colspan="4" class="empty-row">
                        No students match the search criteria.
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <!-- Live Pagination -->
              <div class="live-table-footer">
                <span>Showing {{ paginatedLiveAttendees.length }} of {{ filteredLiveAttendees.length }} students</span>
                <div class="page-btns">
                  <button [disabled]="livePage <= 1" (click)="livePage = livePage - 1">Previous</button>
                  <span>{{ livePage }} / {{ totalLivePages }}</span>
                  <button [disabled]="livePage >= totalLivePages" (click)="livePage = livePage + 1">Next</button>
                </div>
              </div>
            </div>
          </div>

          <ng-template #noLiveSessions>
            <div class="empty-state-box">
              <div class="empty-icon">📡</div>
              <h3>No Active Attendance Session</h3>
              <p>Select one of your assigned courses below to project the dynamic QR attendance screen.</p>
              <div class="quick-launch-grid">
                <div class="quick-course-card" *ngFor="let c of teacherCourses">
                  <div class="qc-top">
                    <span class="code-badge">{{ c.code }}</span>
                    <span class="type-tag">{{ c.type }}</span>
                  </div>
                  <h4>{{ c.title }}</h4>
                  <small>{{ c.schedule }} • {{ c.room }}</small>
                  <button class="btn-launch-sm" (click)="openLaunchModal(c)">
                    ⚡ Start Attendance Session
                  </button>
                </div>
              </div>
            </div>
          </ng-template>
        </section>

        <!-- ======================================================== -->
        <!-- TAB 2: TEACHER COURSES CATALOG & DETAILS MODAL          -->
        <!-- ======================================================== -->
        <section *ngIf="currentTab === 'courses'" class="tab-section">
          <div class="catalog-grid">
            <div class="course-panel-card clickable" *ngFor="let course of teacherCourses">
              <div class="card-accent" [style.background]="course.color"></div>
              <div class="panel-inner">
                <div class="panel-top">
                  <span class="code-badge">{{ course.code }}</span>
                  <span class="type-tag">{{ course.type }}</span>
                  <span class="credits-tag">{{ course.credits }} ECTS</span>
                </div>

                <h3>{{ course.title }}</h3>
                <p class="schedule-text">📅 {{ course.schedule }} • Room {{ course.room }}</p>

                <div class="stats-row">
                  <div class="stat-col">
                    <span class="s-label">Enrolled</span>
                    <strong>28 students</strong>
                  </div>
                  <div class="stat-col">
                    <span class="s-label">Attendance</span>
                    <strong class="text-success">92.5%</strong>
                  </div>
                  <div class="stat-col">
                    <span class="s-label">Lectures</span>
                    <strong>8 held</strong>
                  </div>
                </div>

                <div class="panel-actions">
                  <button class="btn-outline-primary" (click)="openLaunchModal(course)">
                    ⚡ Launch QR Session
                  </button>
                  <button class="btn-outline-neutral" (click)="openCourseDetailsModal(course)">
                    👥 Students & History
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- ======================================================== -->
        <!-- TAB 3: ROSTER & GLOBAL ATTENDANCE MANAGEMENT TABLE       -->
        <!-- ======================================================== -->
        <section *ngIf="currentTab === 'roster'" class="tab-section">
          <!-- Filter and Search Toolbar -->
          <div class="table-toolbar">
            <div class="search-box">
              <span class="icon">🔍</span>
              <input type="text" [(ngModel)]="rosterSearch" placeholder="Search student, major, student ID..." />
            </div>

            <div class="filters-row">
              <select [(ngModel)]="rosterStatusFilter">
                <option value="all">All Statuses</option>
                <option value="Present">Present</option>
                <option value="Late">Late</option>
                <option value="Absent">Absent</option>
              </select>
            </div>
          </div>

          <!-- Advanced Table with Manual Override -->
          <div class="table-card">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Student</th>
                  <th>Specialty</th>
                  <th>Course / Unit</th>
                  <th>Attendance Rate</th>
                  <th>Latest Status</th>
                  <th>Manual Override</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let s of paginatedRoster">
                  <td>
                    <strong>{{ s.name }}</strong>
                    <span class="muted-id">{{ s.studentId }} • {{ s.email }}</span>
                  </td>
                  <td>{{ s.specialty }}</td>
                  <td>{{ s.course }}</td>
                  <td>
                    <div class="progress-bar-wrap">
                      <div class="progress-fill" [style.width.%]="s.attendanceRate"></div>
                      <span class="rate-val">{{ s.attendanceRate }}%</span>
                    </div>
                  </td>
                  <td>
                    <span class="status-tag" [ngClass]="s.status.toLowerCase()">
                      {{ s.status }}
                    </span>
                  </td>
                  <td>
                    <div class="override-btns">
                      <button
                        class="btn-tag present"
                        [class.active]="s.status === 'Present'"
                        (click)="manualChangeStatus(s, 'Present')"
                        title="Mark Present"
                      >
                        P
                      </button>
                      <button
                        class="btn-tag late"
                        [class.active]="s.status === 'Late'"
                        (click)="manualChangeStatus(s, 'Late')"
                        title="Mark Late"
                      >
                        L
                      </button>
                      <button
                        class="btn-tag absent"
                        [class.active]="s.status === 'Absent'"
                        (click)="manualChangeStatus(s, 'Absent')"
                        title="Mark Absent"
                      >
                        A
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>

            <!-- Pagination -->
            <div class="table-pagination">
              <span>Showing {{ paginatedRoster.length }} of {{ filteredRoster.length }} students</span>
              <div class="page-controls">
                <button [disabled]="rosterPage <= 1" (click)="rosterPage = rosterPage - 1">Previous</button>
                <span>Page {{ rosterPage }} / {{ totalRosterPages }}</span>
                <button [disabled]="rosterPage >= totalRosterPages" (click)="rosterPage = rosterPage + 1">Next</button>
              </div>
            </div>
          </div>
        </section>

        <!-- ======================================================== -->
        <!-- TAB 4: REPORTS, SEMESTER ANALYTICS & PRINT SHEETS       -->
        <!-- ======================================================== -->
        <section *ngIf="currentTab === 'reports'" class="tab-section">
          <div class="reports-header">
            <div>
              <h3>Semester Attendance Analytics & Ledgers</h3>
              <p class="subtitle">Analyze semester attendance patterns, identify students at risk, and export official exam eligibility ledgers.</p>
            </div>
            <div class="report-actions-row">
              <button class="btn-print-sheet" (click)="printOfficialSheet()">
                <span>🖨️ Print Official Attendance Sheet</span>
              </button>
              <button class="btn-export-csv" (click)="exportRosterCSV()">
                <span>📥 Export CSV Ledger</span>
              </button>
            </div>
          </div>

          <!-- Semester Trend Cards -->
          <div class="semester-stats-strip">
            <div class="stat-pill-box">
              <span class="p-title">Semester 1 (S1)</span>
              <strong class="text-success">92.4% Attendance</strong>
              <small>12 lectures • 28 students</small>
            </div>
            <div class="stat-pill-box">
              <span class="p-title">Semester 2 (S2)</span>
              <strong>89.1% Attendance</strong>
              <small>8 lectures • 26 students</small>
            </div>
            <div class="stat-pill-box">
              <span class="p-title">Attendance Warning (&lt;70%)</span>
              <strong class="text-danger">1 Student</strong>
              <small>Requires Academic Review</small>
            </div>
          </div>

          <div class="report-summary-cards">
            <div class="report-box" *ngFor="let c of teacherCourses">
              <div class="report-box-top">
                <h4>{{ c.title }} ({{ c.code }})</h4>
                <span class="rate-pill">93.2% attendance</span>
              </div>
              <p>Type: {{ c.type }} • Schedule: {{ c.schedule }} • Room {{ c.room }}</p>
              <div class="mini-stats">
                <span>Total lectures: <strong>12</strong></span>
                <span>Enrolled: <strong>28</strong></span>
                <span>Below 70%: <strong>1</strong></span>
              </div>
              <button class="btn-download-course" (click)="exportCourseCSV(c)">
                Download Official Attendance Ledger (CSV)
              </button>
            </div>
          </div>
        </section>
      </main>

      <!-- ======================================================== -->
      <!-- MODAL: COURSE DETAILS, ENROLLED STUDENTS & SESSIONS     -->
      <!-- ======================================================== -->
      <div class="modal-backdrop" *ngIf="isCourseDetailModalOpen && selectedCourseForDetail as course">
        <div class="modal-window course-modal-wide">
          <div class="modal-header">
            <div>
              <span class="modal-kicker">{{ course.code }} • {{ course.type }}</span>
              <h3>{{ course.title }}</h3>
            </div>
            <button class="btn-close" (click)="isCourseDetailModalOpen = false">✕</button>
          </div>

          <!-- Tabs inside Modal -->
          <div class="modal-subnav">
            <button [class.active]="courseDetailTab === 'students'" (click)="courseDetailTab = 'students'">
              <span>👥 Enrolled Students List ({{ selectedCourseEnrolledStudents.length }})</span>
            </button>
            <button [class.active]="courseDetailTab === 'history'" (click)="courseDetailTab = 'history'">
              <span>📅 Session History ({{ selectedCourseSessions.length }})</span>
            </button>
            <button [class.active]="courseDetailTab === 'details'" (click)="courseDetailTab = 'details'">
              <span>ℹ️ Syllabus & Course Info</span>
            </button>
          </div>

          <div class="modal-body modal-scrollable">
            <!-- 1. Enrolled Students List -->
            <div *ngIf="courseDetailTab === 'students'">
              <table class="data-table compact">
                <thead>
                  <tr>
                    <th>Student ID & Name</th>
                    <th>Specialty</th>
                    <th>Attendance Rate</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let stu of selectedCourseEnrolledStudents">
                    <td>
                      <strong>{{ stu.name }}</strong>
                      <span class="muted-id">{{ stu.studentId }} • {{ stu.email }}</span>
                    </td>
                    <td>{{ stu.specialty }}</td>
                    <td>
                      <div class="progress-bar-wrap">
                        <div class="progress-fill" [style.width.%]="stu.attendanceRate"></div>
                        <span class="rate-val">{{ stu.attendanceRate }}%</span>
                      </div>
                    </td>
                    <td>
                      <span class="status-tag" [ngClass]="stu.status.toLowerCase()">
                        {{ stu.status }}
                      </span>
                    </td>
                  </tr>
                  <tr *ngIf="selectedCourseEnrolledStudents.length === 0">
                    <td colspan="4" class="empty-row">No students enrolled in this course.</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- 2. Session History -->
            <div *ngIf="courseDetailTab === 'history'">
              <div class="session-history-list" *ngIf="selectedCourseSessions.length > 0; else noHistory">
                <div class="history-session-item" *ngFor="let ses of selectedCourseSessions">
                  <div class="ses-left">
                    <span class="ses-badge">{{ ses.sessionType || 'CM' }}</span>
                    <div>
                      <strong>{{ ses.title }}</strong>
                      <small>{{ ses.startedAt | date:'dd/MM/yyyy HH:mm' }} • Room {{ ses.room || 'Hall 3' }}</small>
                    </div>
                  </div>
                  <div class="ses-right">
                    <span class="ses-status-chip" [class.active]="ses.isActive">
                      {{ ses.isActive ? 'Ongoing' : 'Completed' }}
                    </span>
                    <span class="ses-count">👥 {{ ses.attendeesCount || 24 }} attendees</span>
                  </div>
                </div>
              </div>
              <ng-template #noHistory>
                <div class="empty-panel">
                  <p>No past sessions recorded for this course.</p>
                </div>
              </ng-template>
            </div>

            <!-- 3. Syllabus & Info -->
            <div *ngIf="courseDetailTab === 'details'">
              <div class="modal-info-grid">
                <div class="info-block">
                  <span class="label">ECTS Credits</span>
                  <strong>{{ course.credits }} ECTS</strong>
                </div>
                <div class="info-block">
                  <span class="label">Default Room</span>
                  <strong>{{ course.room }}</strong>
                </div>
                <div class="info-block">
                  <span class="label">Weekly Schedule</span>
                  <strong>{{ course.schedule }}</strong>
                </div>
                <div class="info-block">
                  <span class="label">Department / Major</span>
                  <strong>{{ course.department }}</strong>
                </div>
              </div>
              <div class="syllabus-text">
                <h4>Course Description & Learning Outcomes</h4>
                <p>{{ course.description || 'Advanced software engineering course covering architectural patterns, distributed systems, and practical laboratory case studies.' }}</p>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn-secondary" (click)="isCourseDetailModalOpen = false">Close</button>
            <button class="btn-primary" (click)="isCourseDetailModalOpen = false; openLaunchModal(course)">
              ⚡ Start Lecture Session Now
            </button>
          </div>
        </div>
      </div>

      <!-- ======================================================== -->
      <!-- MODAL: LAUNCH NEW QR SESSION                            -->
      <!-- ======================================================== -->
      <div class="modal-backdrop" *ngIf="isLaunchModalOpen && courseToLaunch">
        <div class="modal-window">
          <div class="modal-header">
            <div>
              <span class="modal-kicker">START ATTENDANCE SESSION</span>
              <h3>{{ courseToLaunch.title }} ({{ courseToLaunch.code }})</h3>
            </div>
            <button class="btn-close" (click)="isLaunchModalOpen = false">✕</button>
          </div>

          <div class="modal-body">
            <div class="form-group">
              <label>Session Validity Duration (minutes)</label>
              <input type="number" [(ngModel)]="launchDurationMinutes" min="15" max="180" class="input-modern" />
            </div>

            <div class="form-group">
              <label>Classroom / Lecture Hall</label>
              <input type="text" [(ngModel)]="launchRoom" class="input-modern" />
            </div>

            <div class="form-group">
              <label>Session Type</label>
              <select [(ngModel)]="launchType" class="input-modern">
                <option value="CM">Lecture (CM)</option>
                <option value="TD">Tutorial (TD)</option>
                <option value="TP">Practical Lab (TP)</option>
              </select>
            </div>

            <div class="notice-info">
              💡 Once started, a secure dynamic QR code will be displayed on screen. Students can immediately check in from their dashboard.
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn-secondary" (click)="isLaunchModalOpen = false">Cancel</button>
            <button class="btn-primary" (click)="confirmStartSession()">Start QR Check-in</button>
          </div>
        </div>
      </div>

      <!-- ======================================================== -->
      <!-- MODAL: FULLSCREEN QR PROJECTOR                          -->
      <!-- ======================================================== -->
      <div class="modal-backdrop fullscreen-backdrop" *ngIf="isFullscreenQr && selectedLiveSession as live">
        <div class="fullscreen-qr-box">
          <button class="btn-close-fs" (click)="isFullscreenQr = false">✕ Exit Fullscreen</button>
          <div class="fs-content">
            <span class="fs-badge">PLANORA SECURE ATTENDANCE</span>
            <h2>{{ live.title }}</h2>
            <p class="fs-sub">{{ live.courseCode }} • Room {{ live.room }} • {{ live.teacherName }}</p>

            <div class="big-qr-wrap">
              <img [src]="getQrImage(live)" alt="QR Large" class="big-qr-img" />
            </div>

            <div class="fs-token-display">
              <span>Attendance Token:</span>
              <code>{{ live.qrToken }}</code>
            </div>

            <div class="fs-timer">
              ⏱️ Live Session Active • Automatic close at end of lecture period
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
        color: #0f172a;
        background: #f8fafc;
        min-height: 100vh;
      }
      .teacher-shell { display: flex; flex-direction: column; min-height: 100vh; }
      .top-nav {
        display: flex; align-items: center; justify-content: space-between;
        padding: 0.85rem 2rem; background: #ffffff; border-bottom: 1px solid #e2e8f0;
        position: sticky; top: 0; z-index: 40; box-shadow: 0 1px 3px rgba(0,0,0,0.04);
      }
      .brand { display: flex; align-items: center; gap: 0.75rem; }
      .brand-logo {
        width: 38px; height: 38px; background: linear-gradient(135deg, #4f46e5, #06b6d4);
        color: #fff; font-weight: 800; font-size: 1.25rem; display: flex;
        align-items: center; justify-content: center; border-radius: 10px;
      }
      .brand-title { font-size: 1.15rem; font-weight: 800; color: #0f172a; display: block; line-height: 1.1; }
      .brand-badge { font-size: 0.65rem; font-weight: 700; color: #64748b; }
      .center-tabs { display: flex; gap: 0.4rem; background: #f1f5f9; padding: 0.3rem; border-radius: 12px; }
      .nav-tab {
        border: none; background: transparent; padding: 0.5rem 0.95rem;
        font-size: 0.85rem; font-weight: 600; color: #64748b; border-radius: 8px; cursor: pointer;
        display: flex; align-items: center; gap: 0.4rem; transition: all 0.18s;
      }
      .nav-tab.active { background: #ffffff; color: #4f46e5; box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
      .badge-count { background: #ef4444; color: #fff; font-size: 0.7rem; padding: 0.1rem 0.45rem; border-radius: 9999px; }
      .user-strip { display: flex; align-items: center; gap: 0.85rem; }
      .profile-meta { text-align: right; }
      .user-name { font-weight: 700; font-size: 0.875rem; display: block; }
      .user-sub { font-size: 0.725rem; color: #64748b; }
      .avatar-ring {
        width: 36px; height: 36px; border-radius: 50%; background: #e0e7ff; color: #4338ca;
        display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 0.85rem;
      }

      .dashboard-canvas { max-width: 1380px; margin: 0 auto; width: 100%; padding: 2rem; box-sizing: border-box; }
      .hero-strip { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.75rem; }
      .hero-strip h2 { font-size: 1.65rem; font-weight: 800; margin: 0 0 0.35rem 0; }
      .subtitle { color: #64748b; margin: 0; font-size: 0.925rem; }
      .action-buttons { display: flex; gap: 0.75rem; }
      .btn-refresh { background: #fff; border: 1px solid #cbd5e1; padding: 0.6rem 1rem; border-radius: 8px; font-weight: 600; cursor: pointer; }
      .btn-primary { background: #4f46e5; color: #fff; border: none; padding: 0.6rem 1.25rem; border-radius: 8px; font-weight: 700; cursor: pointer; }

      .kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 1.25rem; margin-bottom: 2rem; }
      .kpi-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; padding: 1.25rem 1.5rem; }
      .kpi-head { display: flex; justify-content: space-between; font-size: 0.8rem; font-weight: 700; color: #64748b; text-transform: uppercase; margin-bottom: 0.5rem; }
      .val { font-size: 2.15rem; font-weight: 800; color: #0f172a; margin-bottom: 0.65rem; }
      .pill-active { background: #fee2e2; color: #b91c1c; padding: 0.2rem 0.5rem; border-radius: 6px; font-size: 0.75rem; font-weight: 700; }
      .pill-idle { background: #f1f5f9; color: #64748b; padding: 0.2rem 0.5rem; border-radius: 6px; font-size: 0.75rem; font-weight: 700; }
      .pill-rate { background: #dcfce7; color: #15803d; padding: 0.2rem 0.5rem; border-radius: 6px; font-size: 0.75rem; font-weight: 700; }
      .pill-info { background: #e0e7ff; color: #4338ca; padding: 0.2rem 0.5rem; border-radius: 6px; font-size: 0.75rem; font-weight: 700; }

      .split-live-layout { display: grid; grid-template-columns: 1.1fr 1.3fr; gap: 1.75rem; }
      .qr-stage-card { background: #fff; border: 2px solid #6366f1; border-radius: 16px; padding: 1.75rem; box-shadow: 0 4px 20px rgba(99,102,241,0.12); }
      .stage-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
      .live-pill { background: #ecfdf5; color: #065f46; font-weight: 800; font-size: 0.75rem; padding: 0.35rem 0.75rem; border-radius: 9999px; display: flex; align-items: center; gap: 0.4rem; }
      .pulse-dot { width: 8px; height: 8px; border-radius: 50%; background: #10b981; animation: pulse-badge 1.5s infinite; }
      .timer-badge { background: #fef3c7; color: #b45309; font-weight: 700; font-size: 0.8rem; padding: 0.35rem 0.75rem; border-radius: 8px; }
      .live-title { font-size: 1.35rem; font-weight: 800; margin: 0 0 0.25rem 0; }
      .live-meta { color: #64748b; font-size: 0.85rem; margin: 0 0 1.25rem 0; }

      .qr-display-frame {
        text-align: center; background: #f8fafc; border: 1px solid #e2e8f0;
        border-radius: 12px; padding: 1.5rem; margin-bottom: 1rem; position: relative;
      }
      .qr-display-frame.locked { filter: grayscale(0.85); opacity: 0.8; }
      .locked-overlay {
        position: absolute; inset: 0; background: rgba(15, 23, 42, 0.75);
        color: #fff; display: flex; flex-direction: column; align-items: center; justify-content: center;
        border-radius: 12px; z-index: 10;
      }
      .lock-icon { font-size: 2rem; margin-bottom: 0.25rem; }
      .qr-img { width: 220px; height: 220px; border-radius: 8px; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.06); margin-bottom: 0.75rem; }
      .qr-token-pill { font-size: 0.85rem; color: #475569; margin-bottom: 0.5rem; }
      .qr-token-pill code { background: #0f172a; color: #38bdf8; padding: 0.25rem 0.6rem; border-radius: 6px; font-weight: 700; }
      .auto-refresh-notice { font-size: 0.75rem; color: #0284c7; font-weight: 600; display: flex; align-items: center; justify-content: center; gap: 0.3rem; }

      .qr-controls-strip {
        display: flex; align-items: center; justify-content: space-between;
        background: #f1f5f9; padding: 0.65rem 0.95rem; border-radius: 10px; margin-bottom: 1.25rem;
      }
      .toggle-control { display: flex; align-items: center; gap: 0.4rem; font-size: 0.8rem; font-weight: 600; color: #334155; cursor: pointer; }
      .btn-toggle-lock {
        background: #0f172a; color: #fff; border: none; font-size: 0.75rem; font-weight: 700;
        padding: 0.35rem 0.75rem; border-radius: 6px; cursor: pointer;
      }
      .btn-toggle-lock.locked { background: #ef4444; }

      .qr-actions { display: flex; gap: 0.5rem; flex-wrap: wrap; }
      .btn-regen { flex: 1; background: #f1f5f9; border: 1px solid #cbd5e1; padding: 0.6rem; border-radius: 8px; font-weight: 700; cursor: pointer; }
      .btn-fullscreen { flex: 1; background: #0f172a; color: #fff; border: none; padding: 0.6rem; border-radius: 8px; font-weight: 700; cursor: pointer; }
      .btn-close-session { width: 100%; background: #fee2e2; color: #b91c1c; border: none; padding: 0.65rem; border-radius: 8px; font-weight: 700; cursor: pointer; margin-top: 0.5rem; }

      .live-roster-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 1.5rem; display: flex; flex-direction: column; }
      .roster-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.85rem; }
      .roster-header h3 { margin: 0; font-size: 1.15rem; font-weight: 700; }
      .roster-header .sub { margin: 0.15rem 0 0 0; font-size: 0.8rem; color: #64748b; }
      .btn-sm-outline { background: #fff; border: 1px solid #cbd5e1; padding: 0.35rem 0.75rem; border-radius: 6px; font-size: 0.75rem; font-weight: 600; cursor: pointer; }

      .live-search-strip { display: flex; gap: 0.5rem; margin-bottom: 0.85rem; }
      .input-live-search { flex: 1; padding: 0.5rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 0.825rem; }
      .select-live-status { padding: 0.5rem 0.75rem; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 0.8rem; background: #fff; }

      .attendees-table-wrap { overflow-y: auto; max-height: 380px; flex: 1; }
      .data-table { width: 100%; border-collapse: collapse; text-align: left; }
      .data-table th { background: #f8fafc; color: #475569; font-size: 0.75rem; font-weight: 700; text-transform: uppercase; padding: 0.65rem 0.85rem; border-bottom: 1px solid #e2e8f0; }
      .data-table td { padding: 0.75rem 0.85rem; font-size: 0.825rem; border-bottom: 1px solid #f1f5f9; }
      .muted-id { display: block; font-size: 0.7rem; color: #94a3b8; }
      .status-tag { font-size: 0.7rem; font-weight: 700; padding: 0.2rem 0.5rem; border-radius: 6px; text-transform: capitalize; }
      .status-tag.present { background: #dcfce7; color: #15803d; }
      .status-tag.late { background: #fef3c7; color: #b45309; }
      .status-tag.absent { background: #fee2e2; color: #b91c1c; }

      .override-btn-group { display: flex; gap: 0.25rem; }
      .ov-btn {
        width: 24px; height: 24px; border-radius: 4px; border: 1px solid #cbd5e1;
        font-weight: 800; font-size: 0.7rem; cursor: pointer; background: #fff;
      }
      .ov-btn.present.active { background: #10b981; color: #fff; border-color: #10b981; }
      .ov-btn.late.active { background: #f59e0b; color: #fff; border-color: #f59e0b; }
      .ov-btn.absent.active { background: #ef4444; color: #fff; border-color: #ef4444; }

      .live-table-footer {
        display: flex; justify-content: space-between; align-items: center;
        padding-top: 0.75rem; border-top: 1px solid #f1f5f9; font-size: 0.75rem; color: #64748b;
      }
      .page-btns { display: flex; align-items: center; gap: 0.5rem; }
      .page-btns button { padding: 0.25rem 0.5rem; background: #fff; border: 1px solid #cbd5e1; border-radius: 4px; font-size: 0.75rem; cursor: pointer; }

      .empty-row { text-align: center; color: #94a3b8; padding: 2rem 1rem; }
      .empty-state-box { background: #fff; border: 1px dashed #cbd5e1; border-radius: 16px; padding: 3.5rem 2rem; text-align: center; }
      .empty-icon { font-size: 3rem; margin-bottom: 0.75rem; }
      .quick-launch-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.25rem; margin-top: 2rem; text-align: left; }
      .quick-course-card { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.25rem; }
      .qc-top { display: flex; justify-content: space-between; margin-bottom: 0.5rem; }
      .code-badge { font-weight: 800; font-size: 0.75rem; background: #0f172a; color: #fff; padding: 0.2rem 0.5rem; border-radius: 6px; }
      .type-tag { font-weight: 700; font-size: 0.75rem; background: #e0e7ff; color: #4338ca; padding: 0.2rem 0.5rem; border-radius: 6px; }
      .quick-course-card h4 { margin: 0 0 0.35rem 0; font-size: 1rem; }
      .quick-course-card small { display: block; color: #64748b; margin-bottom: 1rem; }
      .btn-launch-sm { width: 100%; background: #4f46e5; color: #fff; border: none; padding: 0.6rem; border-radius: 8px; font-weight: 700; font-size: 0.85rem; cursor: pointer; }

      .catalog-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 1.5rem; }
      .course-panel-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; overflow: hidden; display: flex; flex-direction: column; }
      .card-accent { height: 6px; width: 100%; }
      .panel-inner { padding: 1.35rem; display: flex; flex-direction: column; flex: 1; }
      .panel-top { display: flex; gap: 0.5rem; align-items: center; margin-bottom: 0.75rem; }
      .credits-tag { margin-left: auto; font-size: 0.75rem; font-weight: 700; color: #64748b; }
      .course-panel-card h3 { font-size: 1.1rem; font-weight: 700; margin: 0 0 0.35rem 0; }
      .schedule-text { font-size: 0.825rem; color: #64748b; margin: 0 0 1rem 0; }
      .stats-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 0.5rem; background: #f8fafc; padding: 0.75rem; border-radius: 8px; margin-bottom: 1.25rem; }
      .stat-col { text-align: center; }
      .s-label { display: block; font-size: 0.675rem; color: #64748b; text-transform: uppercase; }
      .panel-actions { display: flex; gap: 0.5rem; margin-top: auto; }
      .btn-outline-primary { flex: 1; background: #4f46e5; color: #fff; border: none; padding: 0.55rem; border-radius: 8px; font-weight: 700; font-size: 0.8rem; cursor: pointer; }
      .btn-outline-neutral { flex: 1; background: #fff; border: 1px solid #cbd5e1; padding: 0.55rem; border-radius: 8px; font-weight: 600; font-size: 0.8rem; cursor: pointer; }

      .table-toolbar { display: flex; justify-content: space-between; align-items: center; gap: 1rem; margin-bottom: 1.25rem; flex-wrap: wrap; }
      .search-box { position: relative; flex: 1; min-width: 260px; }
      .search-box .icon { position: absolute; left: 0.85rem; top: 50%; transform: translateY(-50%); color: #94a3b8; }
      .search-box input { width: 100%; padding: 0.65rem 1rem 0.65rem 2.25rem; border: 1px solid #cbd5e1; border-radius: 10px; font-size: 0.85rem; box-sizing: border-box; }
      .filters-row select { padding: 0.6rem 1rem; border: 1px solid #cbd5e1; border-radius: 10px; font-size: 0.85rem; background: #fff; font-weight: 600; }
      .table-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; overflow: hidden; }
      .progress-bar-wrap { position: relative; height: 16px; background: #f1f5f9; border-radius: 9999px; overflow: hidden; width: 110px; }
      .progress-fill { height: 100%; background: #10b981; }
      .rate-val { position: absolute; inset: 0; font-size: 0.675rem; font-weight: 800; display: flex; align-items: center; justify-content: center; color: #0f172a; }
      .override-btns { display: flex; gap: 0.25rem; }
      .btn-tag { width: 24px; height: 24px; border-radius: 4px; border: 1px solid #cbd5e1; font-weight: 800; font-size: 0.7rem; cursor: pointer; background: #fff; }
      .btn-tag.present.active { background: #10b981; color: #fff; border-color: #10b981; }
      .btn-tag.late.active { background: #f59e0b; color: #fff; border-color: #f59e0b; }
      .btn-tag.absent.active { background: #ef4444; color: #fff; border-color: #ef4444; }
      .table-pagination { display: flex; justify-content: space-between; align-items: center; padding: 0.85rem 1.25rem; background: #f8fafc; border-top: 1px solid #e2e8f0; font-size: 0.825rem; color: #64748b; }
      .page-controls { display: flex; align-items: center; gap: 0.75rem; }
      .page-controls button { padding: 0.35rem 0.75rem; background: #fff; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.8rem; cursor: pointer; }

      .reports-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
      .reports-header h3 { font-size: 1.35rem; font-weight: 800; margin: 0 0 0.25rem 0; }
      .report-actions-row { display: flex; gap: 0.75rem; }
      .btn-print-sheet { background: #fff; border: 1px solid #cbd5e1; padding: 0.65rem 1.15rem; border-radius: 8px; font-weight: 700; cursor: pointer; }
      .btn-export-csv { background: #0f172a; color: #fff; border: none; padding: 0.65rem 1.25rem; border-radius: 8px; font-weight: 700; cursor: pointer; }

      .semester-stats-strip {
        display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
        gap: 1.25rem; margin-bottom: 1.75rem;
      }
      .stat-pill-box {
        background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.15rem; text-align: center;
      }
      .p-title { display: block; font-size: 0.75rem; font-weight: 700; color: #64748b; text-transform: uppercase; margin-bottom: 0.25rem; }
      .stat-pill-box strong { font-size: 1.35rem; display: block; margin-bottom: 0.2rem; }
      .stat-pill-box small { color: #94a3b8; font-size: 0.75rem; }
      .text-danger { color: #ef4444 !important; }

      .report-summary-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 1.5rem; }
      .report-box { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; padding: 1.35rem; }
      .report-box-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
      .report-box-top h4 { margin: 0; font-size: 1.05rem; }
      .rate-pill { background: #dcfce7; color: #166534; font-weight: 800; font-size: 0.75rem; padding: 0.2rem 0.55rem; border-radius: 6px; }
      .mini-stats { display: flex; gap: 1rem; font-size: 0.8rem; color: #64748b; margin: 0.75rem 0 1.25rem 0; }
      .btn-download-course { width: 100%; background: #f8fafc; border: 1px solid #cbd5e1; padding: 0.6rem; border-radius: 8px; font-weight: 700; font-size: 0.825rem; cursor: pointer; }

      .modal-backdrop { position: fixed; inset: 0; background: rgba(15,23,42,0.65); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 50; padding: 1.5rem; }
      .modal-window { background: #fff; border-radius: 16px; max-width: 520px; width: 100%; overflow: hidden; box-shadow: 0 20px 25px rgba(0,0,0,0.2); }
      .modal-window.course-modal-wide { max-width: 760px; }
      .modal-header { display: flex; justify-content: space-between; align-items: flex-start; padding: 1.25rem 1.5rem; border-bottom: 1px solid #e2e8f0; }
      .modal-kicker { font-size: 0.725rem; font-weight: 800; color: #4f46e5; }
      .modal-header h3 { margin: 0.2rem 0 0 0; font-size: 1.25rem; }
      .btn-close { background: none; border: none; font-size: 1.25rem; cursor: pointer; color: #94a3b8; }
      .modal-subnav {
        display: flex; gap: 0.5rem; background: #f8fafc; padding: 0.5rem 1.5rem; border-bottom: 1px solid #e2e8f0;
      }
      .modal-subnav button {
        background: none; border: none; padding: 0.45rem 0.75rem; border-radius: 6px;
        font-weight: 600; font-size: 0.8rem; color: #64748b; cursor: pointer;
      }
      .modal-subnav button.active { background: #ffffff; color: #4f46e5; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }

      .modal-body { padding: 1.5rem; }
      .modal-scrollable { max-height: 400px; overflow-y: auto; }
      .modal-info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 1rem; background: #f8fafc; padding: 1rem; border-radius: 10px; margin-bottom: 1.25rem; }
      .info-block .label { display: block; font-size: 0.725rem; color: #64748b; text-transform: uppercase; margin-bottom: 0.2rem; }
      .info-block strong { font-size: 0.9rem; color: #0f172a; }
      .syllabus-text h4 { margin: 0 0 0.5rem 0; font-size: 0.95rem; font-weight: 700; }
      .syllabus-text p { color: #475569; font-size: 0.85rem; line-height: 1.5; margin: 0; }

      .session-history-list { display: flex; flex-direction: column; gap: 0.65rem; }
      .history-session-item {
        display: flex; justify-content: space-between; align-items: center;
        background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 0.75rem 1rem;
      }
      .ses-left { display: flex; align-items: center; gap: 0.75rem; }
      .ses-badge { background: #0f172a; color: #fff; font-size: 0.7rem; font-weight: 800; padding: 0.2rem 0.45rem; border-radius: 4px; }
      .ses-left strong { display: block; font-size: 0.85rem; }
      .ses-left small { color: #64748b; font-size: 0.75rem; }
      .ses-right { text-align: right; }
      .ses-status-chip { font-size: 0.7rem; font-weight: 700; padding: 0.15rem 0.45rem; border-radius: 4px; background: #f1f5f9; color: #64748b; margin-right: 0.5rem; }
      .ses-status-chip.active { background: #dcfce7; color: #166534; }
      .ses-count { font-size: 0.75rem; font-weight: 600; color: #334155; }

      .form-group { margin-bottom: 1.15rem; }
      .form-group label { display: block; font-size: 0.825rem; font-weight: 700; margin-bottom: 0.35rem; color: #334155; }
      .input-modern { width: 100%; padding: 0.65rem 0.85rem; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 0.875rem; box-sizing: border-box; }
      .notice-info { background: #e0f2fe; color: #0369a1; padding: 0.75rem; border-radius: 8px; font-size: 0.8rem; line-height: 1.4; }
      .modal-footer { display: flex; justify-content: flex-end; gap: 0.75rem; padding: 1rem 1.5rem; background: #f8fafc; border-top: 1px solid #e2e8f0; }
      .btn-secondary { background: #fff; border: 1px solid #cbd5e1; padding: 0.55rem 1rem; border-radius: 8px; font-weight: 600; cursor: pointer; }

      .fullscreen-backdrop { background: rgba(15,23,42,0.95); }
      .fullscreen-qr-box { position: relative; max-width: 650px; width: 100%; text-align: center; color: #fff; }
      .btn-close-fs { position: absolute; right: 0; top: -3rem; background: rgba(255,255,255,0.2); border: none; color: #fff; padding: 0.5rem 1rem; border-radius: 8px; cursor: pointer; font-weight: 700; }
      .fs-badge { font-size: 0.75rem; font-weight: 800; color: #38bdf8; letter-spacing: 0.05em; }
      .fullscreen-qr-box h2 { font-size: 2rem; font-weight: 800; margin: 0.5rem 0 0.25rem 0; }
      .fs-sub { color: #94a3b8; margin: 0 0 1.5rem 0; font-size: 1rem; }
      .big-qr-wrap { background: #fff; padding: 2rem; border-radius: 20px; display: inline-block; box-shadow: 0 0 50px rgba(99,102,241,0.4); margin-bottom: 1.25rem; }
      .big-qr-img { width: 320px; height: 320px; border-radius: 12px; }
      .fs-token-display { font-size: 1.1rem; color: #cbd5e1; margin-bottom: 1rem; }
      .fs-token-display code { background: #1e293b; color: #38bdf8; padding: 0.35rem 0.85rem; border-radius: 8px; font-weight: 800; margin-left: 0.5rem; }
      .fs-timer { color: #94a3b8; font-size: 0.85rem; }

      @keyframes pulse-badge { 0% { transform: scale(1); opacity: 1; } 50% { transform: scale(1.15); opacity: 0.85; } 100% { transform: scale(1); opacity: 1; } }

      @media (max-width: 900px) {
        .split-live-layout { grid-template-columns: 1fr; }
        .top-nav { flex-direction: column; gap: 0.75rem; }
        .center-tabs { width: 100%; overflow-x: auto; }
      }
    `
  ]
})
export class TeacherAttendanceComponent implements OnInit, OnDestroy {
  currentTab: 'live' | 'courses' | 'roster' | 'reports' = 'live';

  teacherCourses: CourseCatalogItem[] = [];
  activeSessions: AttendanceSession[] = [];
  selectedLiveSession: AttendanceSession | null = null;
  liveAttendees: AttendanceRecord[] = [];

  // Live session controls & filters
  isAutoRefreshEnabled = true;
  isSessionLocked = false;
  liveSearchTerm = '';
  liveStatusFilter = 'all';
  livePage = 1;
  livePageSize = 5;

  // Course Details Modal State
  isCourseDetailModalOpen = false;
  selectedCourseForDetail: CourseCatalogItem | null = null;
  selectedCourseEnrolledStudents: any[] = [];
  selectedCourseSessions: AttendanceSession[] = [];
  courseDetailTab: 'details' | 'students' | 'history' = 'students';

  // Global Roster Filter & Pagination
  roster: TeacherStudentRecord[] = [];
  rosterSearch = '';
  rosterStatusFilter = 'all';
  rosterPage = 1;
  rosterPageSize = 6;

  // Modal Launch state
  isLaunchModalOpen = false;
  courseToLaunch: CourseCatalogItem | null = null;
  launchDurationMinutes = 90;
  launchRoom = 'Hall 3';
  launchType = 'CM';

  // Fullscreen state
  isFullscreenQr = false;

  private pollSub?: Subscription;
  private autoRotateSub?: Subscription;

  constructor(private attendanceService: AttendanceService) {}

  ngOnInit(): void {
    this.refreshAll();

    // Poll live status every 8 seconds
    this.pollSub = interval(8000).subscribe(() => {
      this.refreshLiveStatus();
    });

    // Auto rotate dynamic QR token every 30 seconds if enabled
    this.autoRotateSub = interval(30000).subscribe(() => {
      if (this.selectedLiveSession && this.isAutoRefreshEnabled && !this.isSessionLocked) {
        this.regenerateSession(this.selectedLiveSession);
      }
    });
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
    this.autoRotateSub?.unsubscribe();
  }

  refreshAll(): void {
    this.teacherCourses = this.attendanceService.getTeacherCourses('TEACH-01');
    this.roster = this.attendanceService.getTeacherRoster('TEACH-01');
    this.refreshLiveStatus();
  }

  refreshLiveStatus(): void {
    this.attendanceService.getActiveSessionsHttp('TEACH-01').subscribe({
      next: (sessions) => {
        this.activeSessions = sessions;
        if (sessions.length > 0) {
          if (!this.selectedLiveSession || !sessions.some((s) => s.id === this.selectedLiveSession!.id)) {
            this.selectedLiveSession = sessions[0];
          }
          this.loadAttendees(this.selectedLiveSession.id);
        } else {
          this.selectedLiveSession = null;
          this.liveAttendees = [];
        }
      },
      error: () => {
        this.activeSessions = this.attendanceService.getState().sessions.filter((s) => s.isActive);
        if (this.activeSessions.length > 0) {
          this.selectedLiveSession = this.activeSessions[0];
          this.liveAttendees = this.attendanceService.getState().records.filter((r) => r.sessionId === this.selectedLiveSession!.id);
        }
      }
    });
  }

  loadAttendees(sessionId: string): void {
    this.attendanceService.getSessionAttendeesHttp(sessionId).subscribe({
      next: (records) => {
        this.liveAttendees = records;
      },
      error: () => {
        this.liveAttendees = this.attendanceService.getState().records.filter((r) => r.sessionId === sessionId);
      }
    });
  }

  get totalEnrolledStudents(): number {
    return 28;
  }

  get currentSessionRate(): number {
    if (!this.totalEnrolledStudents) return 0;
    return Math.round((this.liveAttendees.length / this.totalEnrolledStudents) * 100);
  }

  getQrImage(session: AttendanceSession): string {
    return this.attendanceService.getQrImageUrl(session);
  }

  // ==========================================
  // COURSE DETAILS, ENROLLED STUDENTS & SESSIONS
  // ==========================================

  openCourseDetailsModal(course: CourseCatalogItem): void {
    this.selectedCourseForDetail = course;
    this.courseDetailTab = 'students';
    this.isCourseDetailModalOpen = true;

    const courseNumId = Number(course.id.replace('course-', '')) || 1;
    this.attendanceService.getCourseStudentsHttp(courseNumId).subscribe({
      next: (students) => {
        this.selectedCourseEnrolledStudents = students;
      },
      error: () => {
        this.selectedCourseEnrolledStudents = this.roster.filter((s) => s.course === course.title || true);
      }
    });

    this.attendanceService.getCourseSessionsHttp(courseNumId).subscribe({
      next: (sessions) => {
        this.selectedCourseSessions = sessions;
      },
      error: () => {
        this.selectedCourseSessions = this.activeSessions.filter((s) => s.courseId === course.id);
      }
    });
  }

  // ==========================================
  // LIVE QR LAUNCH & REGENERATION
  // ==========================================

  openLaunchModal(course: CourseCatalogItem): void {
    this.courseToLaunch = course;
    this.launchRoom = course.room || 'Hall 3';
    this.launchType = course.type || 'CM';
    this.isLaunchModalOpen = true;
  }

  confirmStartSession(): void {
    if (!this.courseToLaunch) return;

    this.attendanceService
      .startSessionHttp({
        courseId: Number(this.courseToLaunch.id.replace('course-', '')) || 1,
        durationMinutes: this.launchDurationMinutes,
        room: this.launchRoom,
        sessionType: this.launchType
      })
      .subscribe(() => {
        this.isLaunchModalOpen = false;
        this.currentTab = 'live';
        this.refreshAll();
      });
  }

  regenerateSession(session: AttendanceSession): void {
    this.attendanceService.regenerateQrHttp(session.id).subscribe(() => {
      this.refreshAll();
    });
  }

  closeSession(session: AttendanceSession): void {
    this.attendanceService.closeSessionHttp(session.id).subscribe(() => {
      this.refreshAll();
    });
  }

  openFullscreen(session: AttendanceSession): void {
    this.selectedLiveSession = session;
    this.isFullscreenQr = true;
  }

  // ==========================================
  // LIVE ATTENDEES TABLE FILTERS & OVERRIDES
  // ==========================================

  get filteredLiveAttendees(): AttendanceRecord[] {
    const term = this.liveSearchTerm.trim().toLowerCase();
    return this.liveAttendees.filter((a) => {
      const matchSearch = !term || a.studentName.toLowerCase().includes(term) || a.studentId.toLowerCase().includes(term);
      const matchStatus = this.liveStatusFilter === 'all' || a.status.toLowerCase() === this.liveStatusFilter.toLowerCase();
      return matchSearch && matchStatus;
    });
  }

  get paginatedLiveAttendees(): AttendanceRecord[] {
    const start = (this.livePage - 1) * this.livePageSize;
    return this.filteredLiveAttendees.slice(start, start + this.livePageSize);
  }

  get totalLivePages(): number {
    return Math.max(1, Math.ceil(this.filteredLiveAttendees.length / this.livePageSize));
  }

  setManualStatus(att: AttendanceRecord, status: 'PRESENT' | 'LATE' | 'ABSENT'): void {
    this.attendanceService.overrideAttendanceHttp(att.id, status).subscribe(() => {
      att.status = status.toLowerCase() as any;
    });
  }

  manualChangeStatus(student: TeacherStudentRecord, newStatus: 'Present' | 'Late' | 'Absent'): void {
    student.status = newStatus;
  }

  // ==========================================
  // GLOBAL ROSTER FILTERS & PAGINATION
  // ==========================================

  get filteredRoster(): TeacherStudentRecord[] {
    const term = this.rosterSearch.trim().toLowerCase();
    return this.roster.filter((s) => {
      const matchSearch = !term || s.name.toLowerCase().includes(term) || s.specialty.toLowerCase().includes(term);
      const matchStatus = this.rosterStatusFilter === 'all' || s.status === this.rosterStatusFilter;
      return matchSearch && matchStatus;
    });
  }

  get paginatedRoster(): TeacherStudentRecord[] {
    const start = (this.rosterPage - 1) * this.rosterPageSize;
    return this.filteredRoster.slice(start, start + this.rosterPageSize);
  }

  get totalRosterPages(): number {
    return Math.max(1, Math.ceil(this.filteredRoster.length / this.rosterPageSize));
  }

  // ==========================================
  // REPORTS & EXPORTS
  // ==========================================

  exportRosterCSV(): void {
    const rows = [
      ['Student ID', 'Full Name', 'Email', 'Specialty', 'Course', 'Attendance Rate', 'Status'],
      ...this.roster.map((s) => [s.studentId, s.name, s.email, s.specialty, s.course, `${s.attendanceRate}%`, s.status])
    ];
    this.downloadCSV(rows, 'planora_global_attendance_ledger.csv');
  }

  exportCourseCSV(course: CourseCatalogItem): void {
    const rows = [
      ['Course', 'Code', 'Specialty', 'Room', 'Student', 'Attendance Rate %'],
      ['Advanced Software Architecture', 'CS301', 'Software Engineering', 'Hall 3', 'Alexandre Mbida', '94%'],
      ['Advanced Software Architecture', 'CS301', 'Software Engineering', 'Hall 3', 'Carine Ngo', '88%'],
      ['Advanced Software Architecture', 'CS301', 'Software Engineering', 'Hall 3', 'David Fokam', '72%']
    ];
    this.downloadCSV(rows, `planora_attendance_${course.code}.csv`);
  }

  printOfficialSheet(): void {
    window.print();
  }

  private downloadCSV(rows: string[][], filename: string): void {
    const csvContent = 'data:text/csv;charset=utf-8,' + rows.map((e) => e.join(',')).join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}
