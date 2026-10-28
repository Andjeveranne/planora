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
            <span class="brand-badge">ESPACE ENSEIGNANT</span>
          </div>
        </div>

        <div class="center-tabs">
          <button class="nav-tab" [class.active]="currentTab === 'live'" (click)="currentTab = 'live'">
            <span>⚡ Sessions Live & QR</span>
            <span class="badge-count" *ngIf="activeSessions.length > 0">{{ activeSessions.length }}</span>
          </button>
          <button class="nav-tab" [class.active]="currentTab === 'courses'" (click)="currentTab = 'courses'">
            <span>📚 Mes Cours ({{ teacherCourses.length }})</span>
          </button>
          <button class="nav-tab" [class.active]="currentTab === 'roster'" (click)="currentTab = 'roster'">
            <span>👥 Roster & Émargements</span>
          </button>
          <button class="nav-tab" [class.active]="currentTab === 'reports'" (click)="currentTab = 'reports'">
            <span>📊 Rapports & Analyses</span>
          </button>
        </div>

        <div class="user-strip">
          <div class="profile-meta">
            <span class="user-name">Dr. Jean Dupont</span>
            <span class="user-sub">Département Génie Logiciel</span>
          </div>
          <div class="avatar-ring">JD</div>
        </div>
      </header>

      <!-- Main Canvas -->
      <main class="dashboard-canvas">
        <!-- Hero Header Strip -->
        <div class="hero-strip">
          <div>
            <h2>Gestion de Présence & Émargement par QR Code</h2>
            <p class="subtitle">Générez des QR codes dynamiques synchronisés et suivez les émargements en temps réel.</p>
          </div>
          <div class="action-buttons">
            <button class="btn-refresh" (click)="refreshAll()">
              <span>🔄 Actualiser</span>
            </button>
            <button class="btn-primary" (click)="openLaunchModal(teacherCourses[0])" *ngIf="teacherCourses.length > 0">
              <span>⚡ Lancer une session QR</span>
            </button>
          </div>
        </div>

        <!-- 3 Quick KPI Strips -->
        <div class="kpi-grid">
          <div class="kpi-card purple">
            <div class="kpi-head">
              <span class="label">Sessions en direct</span>
              <span class="icon">📡</span>
            </div>
            <div class="val">{{ activeSessions.length }}</div>
            <div class="desc">
              <span class="pill-active" *ngIf="activeSessions.length > 0">Émargement en cours</span>
              <span class="pill-idle" *ngIf="activeSessions.length === 0">Aucune session en cours</span>
            </div>
          </div>

          <div class="kpi-card green">
            <div class="kpi-head">
              <span class="label">Étudiants présents aujourd'hui</span>
              <span class="icon">👥</span>
            </div>
            <div class="val">{{ totalPresentToday }}</div>
            <div class="desc">
              <span class="pill-rate">Taux moyen : 91.4%</span>
            </div>
          </div>

          <div class="kpi-card blue">
            <div class="kpi-head">
              <span class="label">Cours assignés</span>
              <span class="icon">📖</span>
            </div>
            <div class="val">{{ teacherCourses.length }}</div>
            <div class="desc">
              <span class="pill-info">Semestre Académique S1</span>
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
                  <span>EN DIRECT : {{ live.courseCode }}</span>
                </div>
                <div class="timer-badge">
                  ⏱️ Expire dans {{ live.remainingSeconds ? (live.remainingSeconds / 60 | number:'1.0-0') : 45 }} min
                </div>
              </div>

              <h3 class="live-title">{{ live.title }}</h3>
              <p class="live-meta">📍 Salle : {{ live.room || 'Amphi 3' }} • Type : {{ live.sessionType || 'CM' }}</p>

              <!-- Dynamic QR Code Container -->
              <div class="qr-display-frame">
                <img [src]="getQrImage(live)" alt="Dynamic Attendance QR" class="qr-img" />
                <div class="qr-token-pill">
                  <span>Token : </span>
                  <code>{{ live.qrToken }}</code>
                </div>
              </div>

              <!-- Controls -->
              <div class="qr-actions">
                <button class="btn-regen" (click)="regenerateSession(live)">
                  <span>🔄 Régénérer QR Code</span>
                </button>
                <button class="btn-fullscreen" (click)="openFullscreen(live)">
                  <span>🔍 Plein écran</span>
                </button>
                <button class="btn-close-session" (click)="closeSession(live)">
                  <span>⏹️ Clôturer la séance</span>
                </button>
              </div>
            </div>

            <!-- Right: Real-time Live Attendees Feed -->
            <div class="live-roster-card" *ngIf="selectedLiveSession as live">
              <div class="roster-header">
                <div>
                  <h3>Émargements en temps réel ({{ liveAttendees.length }})</h3>
                  <p class="sub">Les étudiants scannés apparaissent instantanément</p>
                </div>
                <button class="btn-sm-outline" (click)="loadAttendees(live.id)">Actualiser</button>
              </div>

              <div class="attendees-table-wrap">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>Étudiant</th>
                      <th>Statut</th>
                      <th>Heure</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr *ngFor="let att of liveAttendees">
                      <td>
                        <strong>{{ att.studentName }}</strong>
                        <span class="muted-id">{{ att.studentId }}</span>
                      </td>
                      <td>
                        <span class="status-tag" [ngClass]="att.status.toLowerCase()">
                          {{ att.status }}
                        </span>
                      </td>
                      <td>{{ att.scannedAt | date:'HH:mm:ss' }}</td>
                      <td>
                        <button class="btn-override" (click)="toggleOverride(att)">
                          {{ att.status === 'present' ? 'Marquer absent' : 'Marquer présent' }}
                        </button>
                      </td>
                    </tr>
                    <tr *ngIf="liveAttendees.length === 0">
                      <td colspan="4" class="empty-row">
                        En attente du premier émargement étudiant...
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <ng-template #noLiveSessions>
            <div class="empty-state-box">
              <div class="empty-icon">📡</div>
              <h3>Aucune session d'émargement active</h3>
              <p>Sélectionnez un de vos cours ci-dessous pour lancer la projection du QR code dynamique.</p>
              <div class="quick-launch-grid">
                <div class="quick-course-card" *ngFor="let c of teacherCourses">
                  <div class="qc-top">
                    <span class="code-badge">{{ c.code }}</span>
                    <span class="type-tag">{{ c.type }}</span>
                  </div>
                  <h4>{{ c.title }}</h4>
                  <small>{{ c.schedule }} • {{ c.room }}</small>
                  <button class="btn-launch-sm" (click)="openLaunchModal(c)">
                    ⚡ Démarrer l'émargement
                  </button>
                </div>
              </div>
            </div>
          </ng-template>
        </section>

        <!-- ======================================================== -->
        <!-- TAB 2: TEACHER COURSES CATALOG                          -->
        <!-- ======================================================== -->
        <section *ngIf="currentTab === 'courses'" class="tab-section">
          <div class="catalog-grid">
            <div class="course-panel-card" *ngFor="let course of teacherCourses">
              <div class="card-accent" [style.background]="course.color"></div>
              <div class="panel-inner">
                <div class="panel-top">
                  <span class="code-badge">{{ course.code }}</span>
                  <span class="type-tag">{{ course.type }}</span>
                  <span class="credits-tag">{{ course.credits }} ECTS</span>
                </div>

                <h3>{{ course.title }}</h3>
                <p class="schedule-text">📅 {{ course.schedule }} • Salle {{ course.room }}</p>

                <div class="stats-row">
                  <div class="stat-col">
                    <span class="s-label">Étudiants</span>
                    <strong>28 inscrits</strong>
                  </div>
                  <div class="stat-col">
                    <span class="s-label">Assiduité</span>
                    <strong class="text-success">92.5%</strong>
                  </div>
                  <div class="stat-col">
                    <span class="s-label">Séances</span>
                    <strong>8 complétées</strong>
                  </div>
                </div>

                <div class="panel-actions">
                  <button class="btn-outline-primary" (click)="openLaunchModal(course)">
                    ⚡ Lancer session QR
                  </button>
                  <button class="btn-outline-neutral" (click)="selectCourseForReport(course)">
                    📊 Rapport
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- ======================================================== -->
        <!-- TAB 3: ROSTER & ATTENDANCE MANAGEMENT TABLE              -->
        <!-- ======================================================== -->
        <section *ngIf="currentTab === 'roster'" class="tab-section">
          <!-- Filter and Search Toolbar -->
          <div class="table-toolbar">
            <div class="search-box">
              <span class="icon">🔍</span>
              <input type="text" [(ngModel)]="rosterSearch" placeholder="Rechercher étudiant, spécialité..." />
            </div>

            <div class="filters-row">
              <select [(ngModel)]="rosterStatusFilter">
                <option value="all">Tous les statuts</option>
                <option value="Present">Présents</option>
                <option value="Late">Retards</option>
                <option value="Absent">Absents</option>
              </select>
            </div>
          </div>

          <!-- Advanced Table with Manual Override -->
          <div class="table-card">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Étudiant</th>
                  <th>Spécialité</th>
                  <th>Matière</th>
                  <th>Taux d'assiduité</th>
                  <th>Dernier Statut</th>
                  <th>Override Manuel</th>
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
                      >
                        P
                      </button>
                      <button
                        class="btn-tag late"
                        [class.active]="s.status === 'Late'"
                        (click)="manualChangeStatus(s, 'Late')"
                      >
                        R
                      </button>
                      <button
                        class="btn-tag absent"
                        [class.active]="s.status === 'Absent'"
                        (click)="manualChangeStatus(s, 'Absent')"
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
              <span>Affichage de {{ paginatedRoster.length }} sur {{ filteredRoster.length }} étudiants</span>
              <div class="page-controls">
                <button [disabled]="rosterPage <= 1" (click)="rosterPage = rosterPage - 1">Précédent</button>
                <span>Page {{ rosterPage }} / {{ totalRosterPages }}</span>
                <button [disabled]="rosterPage >= totalRosterPages" (click)="rosterPage = rosterPage + 1">Suivant</button>
              </div>
            </div>
          </div>
        </section>

        <!-- ======================================================== -->
        <!-- TAB 4: REPORTS & ANALYTICS                               -->
        <!-- ======================================================== -->
        <section *ngIf="currentTab === 'reports'" class="tab-section">
          <div class="reports-header">
            <div>
              <h3>Relevés d'assiduité & Statistiques d'amphithéâtre</h3>
              <p class="subtitle">Téléchargez les rapports d'assiduité officiels pour le jury d'examen.</p>
            </div>
            <button class="btn-export-csv" (click)="exportRosterCSV()">
              <span>📥 Exporter le rapport complet (CSV)</span>
            </button>
          </div>

          <div class="report-summary-cards">
            <div class="report-box" *ngFor="let c of teacherCourses">
              <div class="report-box-top">
                <h4>{{ c.title }} ({{ c.code }})</h4>
                <span class="rate-pill">93.2% présence</span>
              </div>
              <p>Type : {{ c.type }} • Créneau : {{ c.schedule }} • Salle {{ c.room }}</p>
              <div class="mini-stats">
                <span>Total séances : <strong>12</strong></span>
                <span>Inscrits : <strong>28</strong></span>
                <span>Défaillants (<70%) : <strong>1</strong></span>
              </div>
              <button class="btn-download-course" (click)="exportCourseCSV(c)">
                Télécharger le relevé de notes & présence
              </button>
            </div>
          </div>
        </section>
      </main>

      <!-- ======================================================== -->
      <!-- MODAL: LAUNCH NEW QR SESSION                            -->
      <!-- ======================================================== -->
      <div class="modal-backdrop" *ngIf="isLaunchModalOpen && courseToLaunch">
        <div class="modal-window">
          <div class="modal-header">
            <div>
              <span class="modal-kicker">NOUVELLE SESSION D'ÉMARGEMENT</span>
              <h3>{{ courseToLaunch.title }} ({{ courseToLaunch.code }})</h3>
            </div>
            <button class="btn-close" (click)="isLaunchModalOpen = false">✕</button>
          </div>

          <div class="modal-body">
            <div class="form-group">
              <label>Durée de validité de la session (minutes)</label>
              <input type="number" [(ngModel)]="launchDurationMinutes" min="15" max="180" class="input-modern" />
            </div>

            <div class="form-group">
              <label>Salle de cours</label>
              <input type="text" [(ngModel)]="launchRoom" class="input-modern" />
            </div>

            <div class="form-group">
              <label>Type de session</label>
              <select [(ngModel)]="launchType" class="input-modern">
                <option value="CM">Cours Magistral (CM)</option>
                <option value="TD">Travaux Dirigés (TD)</option>
                <option value="TP">Travaux Pratiques (TP)</option>
              </select>
            </div>

            <div class="notice-info">
              💡 Une fois la session démarrée, un code QR dynamique sera projeté à l'écran. Les étudiants pourront le scanner immédiatement.
            </div>
          </div>

          <div class="modal-footer">
            <button class="btn-secondary" (click)="isLaunchModalOpen = false">Annuler</button>
            <button class="btn-primary" (click)="confirmStartSession()">Lancer la projection QR</button>
          </div>
        </div>
      </div>

      <!-- ======================================================== -->
      <!-- MODAL: FULLSCREEN QR PROJECTOR                          -->
      <!-- ======================================================== -->
      <div class="modal-backdrop fullscreen-backdrop" *ngIf="isFullscreenQr && selectedLiveSession as live">
        <div class="fullscreen-qr-box">
          <button class="btn-close-fs" (click)="isFullscreenQr = false">✕ Quitter le plein écran</button>
          <div class="fs-content">
            <span class="fs-badge">PLANORA ÉMARGEMENT SÉCURISÉ</span>
            <h2>{{ live.title }}</h2>
            <p class="fs-sub">{{ live.courseCode }} • Salle {{ live.room }} • {{ live.teacherName }}</p>

            <div class="big-qr-wrap">
              <img [src]="getQrImage(live)" alt="QR Large" class="big-qr-img" />
            </div>

            <div class="fs-token-display">
              <span>Token de présence :</span>
              <code>{{ live.qrToken }}</code>
            </div>

            <div class="fs-timer">
              ⏱️ Session active • Clôture automatique à la fin du cours
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
      .qr-display-frame { text-align: center; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 1.5rem; margin-bottom: 1.25rem; }
      .qr-img { width: 220px; height: 220px; border-radius: 8px; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.06); margin-bottom: 0.75rem; }
      .qr-token-pill { font-size: 0.85rem; color: #475569; }
      .qr-token-pill code { background: #0f172a; color: #38bdf8; padding: 0.25rem 0.6rem; border-radius: 6px; font-weight: 700; }
      .qr-actions { display: flex; gap: 0.5rem; flex-wrap: wrap; }
      .btn-regen { flex: 1; background: #f1f5f9; border: 1px solid #cbd5e1; padding: 0.6rem; border-radius: 8px; font-weight: 700; cursor: pointer; }
      .btn-fullscreen { flex: 1; background: #0f172a; color: #fff; border: none; padding: 0.6rem; border-radius: 8px; font-weight: 700; cursor: pointer; }
      .btn-close-session { width: 100%; background: #fee2e2; color: #b91c1c; border: none; padding: 0.65rem; border-radius: 8px; font-weight: 700; cursor: pointer; margin-top: 0.5rem; }

      .live-roster-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 1.5rem; display: flex; flex-direction: column; }
      .roster-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem; }
      .roster-header h3 { margin: 0; font-size: 1.15rem; font-weight: 700; }
      .roster-header .sub { margin: 0.15rem 0 0 0; font-size: 0.8rem; color: #64748b; }
      .btn-sm-outline { background: #fff; border: 1px solid #cbd5e1; padding: 0.35rem 0.75rem; border-radius: 6px; font-size: 0.75rem; font-weight: 600; cursor: pointer; }
      .attendees-table-wrap { overflow-y: auto; max-height: 440px; }

      .data-table { width: 100%; border-collapse: collapse; text-align: left; }
      .data-table th { background: #f8fafc; color: #475569; font-size: 0.775rem; font-weight: 700; text-transform: uppercase; padding: 0.75rem 1rem; border-bottom: 1px solid #e2e8f0; }
      .data-table td { padding: 0.85rem 1rem; font-size: 0.85rem; border-bottom: 1px solid #f1f5f9; }
      .muted-id { display: block; font-size: 0.725rem; color: #94a3b8; }
      .status-tag { font-size: 0.725rem; font-weight: 700; padding: 0.2rem 0.55rem; border-radius: 6px; text-transform: capitalize; }
      .status-tag.present { background: #dcfce7; color: #15803d; }
      .status-tag.late { background: #fef3c7; color: #b45309; }
      .status-tag.absent { background: #fee2e2; color: #b91c1c; }
      .btn-override { background: #f1f5f9; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 0.75rem; font-weight: 600; padding: 0.25rem 0.6rem; cursor: pointer; }
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
      .btn-export-csv { background: #0f172a; color: #fff; border: none; padding: 0.65rem 1.25rem; border-radius: 8px; font-weight: 700; cursor: pointer; }
      .report-summary-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 1.5rem; }
      .report-box { background: #fff; border: 1px solid #e2e8f0; border-radius: 14px; padding: 1.35rem; }
      .report-box-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
      .report-box-top h4 { margin: 0; font-size: 1.05rem; }
      .rate-pill { background: #dcfce7; color: #166534; font-weight: 800; font-size: 0.75rem; padding: 0.2rem 0.55rem; border-radius: 6px; }
      .mini-stats { display: flex; gap: 1rem; font-size: 0.8rem; color: #64748b; margin: 0.75rem 0 1.25rem 0; }
      .btn-download-course { width: 100%; background: #f8fafc; border: 1px solid #cbd5e1; padding: 0.6rem; border-radius: 8px; font-weight: 700; font-size: 0.825rem; cursor: pointer; }

      .modal-backdrop { position: fixed; inset: 0; background: rgba(15,23,42,0.65); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; z-index: 50; padding: 1.5rem; }
      .modal-window { background: #fff; border-radius: 16px; max-width: 520px; width: 100%; overflow: hidden; box-shadow: 0 20px 25px rgba(0,0,0,0.2); }
      .modal-header { display: flex; justify-content: space-between; align-items: flex-start; padding: 1.25rem 1.5rem; border-bottom: 1px solid #e2e8f0; }
      .modal-kicker { font-size: 0.725rem; font-weight: 800; color: #4f46e5; }
      .modal-header h3 { margin: 0.2rem 0 0 0; font-size: 1.25rem; }
      .btn-close { background: none; border: none; font-size: 1.25rem; cursor: pointer; color: #94a3b8; }
      .modal-body { padding: 1.5rem; }
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

  roster: TeacherStudentRecord[] = [];
  rosterSearch = '';
  rosterStatusFilter = 'all';
  rosterPage = 1;
  rosterPageSize = 6;

  // Modal Launch state
  isLaunchModalOpen = false;
  courseToLaunch: CourseCatalogItem | null = null;
  launchDurationMinutes = 90;
  launchRoom = 'Amphi 3';
  launchType = 'CM';

  // Fullscreen state
  isFullscreenQr = false;

  private pollSub?: Subscription;

  constructor(private attendanceService: AttendanceService) {}

  ngOnInit(): void {
    this.refreshAll();

    // Poll live sessions and attendees every 8 seconds
    this.pollSub = interval(8000).subscribe(() => {
      this.refreshLiveStatus();
    });
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
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

  get totalPresentToday(): number {
    return this.liveAttendees.filter((a) => a.status.toLowerCase() === 'present').length + 24;
  }

  getQrImage(session: AttendanceSession): string {
    return this.attendanceService.getQrImageUrl(session);
  }

  openLaunchModal(course: CourseCatalogItem): void {
    this.courseToLaunch = course;
    this.launchRoom = course.room || 'Amphi 3';
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

  toggleOverride(att: AttendanceRecord): void {
    const nextStatus = att.status.toLowerCase() === 'present' ? 'ABSENT' : 'PRESENT';
    this.attendanceService.overrideAttendanceHttp(att.id, nextStatus as any).subscribe(() => {
      att.status = nextStatus.toLowerCase() as any;
    });
  }

  manualChangeStatus(student: TeacherStudentRecord, newStatus: 'Present' | 'Late' | 'Absent'): void {
    student.status = newStatus;
  }

  // Roster Filter & Pagination
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

  selectCourseForReport(course: CourseCatalogItem): void {
    this.currentTab = 'reports';
  }

  exportRosterCSV(): void {
    const rows = [
      ['ID Etudiant', 'Nom', 'Email', 'Specialite', 'Cours', 'Taux Presence', 'Statut'],
      ...this.roster.map((s) => [s.studentId, s.name, s.email, s.specialty, s.course, `${s.attendanceRate}%`, s.status])
    ];
    this.downloadCSV(rows, 'planora_rapport_presence_enseignant.csv');
  }

  exportCourseCSV(course: CourseCatalogItem): void {
    const rows = [
      ['Cours', 'Code', 'Specialite', 'Salle', 'Etudiant', 'Presence %'],
      ['Advanced Software Architecture', 'CS301', 'Software Engineering', 'Amphi 3', 'Etudiant Planora', '94%'],
      ['Advanced Software Architecture', 'CS301', 'Software Engineering', 'Amphi 3', 'Alexandre Mbida', '88%'],
      ['Advanced Software Architecture', 'CS301', 'Software Engineering', 'Amphi 3', 'Carine Ngo', '72%']
    ];
    this.downloadCSV(rows, `planora_presence_${course.code}.csv`);
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
