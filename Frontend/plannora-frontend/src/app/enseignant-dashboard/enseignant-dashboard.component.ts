import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { PlanningService, EmploiDuTempsDTO, DisponibiliteDTO } from '../services/planning.service';
import { EmploiDuTempsService, UEEnseignantDTO } from '../services/emploi-du-temps.service';
import { NotificationService } from '../services/notification.service';
import { NotificationApiService, TeacherNotification } from '../services/notification-api.service';
import { GestionDisponibilitesComponent } from './gestion-disponibilites/gestion-disponibilites.component';
import { EcoleService } from '../services/ecole.service';
import { TeacherAttendanceComponent } from '../teacher-attendance/teacher-attendance.component';
import { UEService, UEDTO } from '../services/ue.service';
import { CourseService, CourseDTO } from '../services/course.service';
import { AttendanceService } from '../services/attendance.service';

interface UserInfo {
  nom: string;
  prenom: string;
  email: string;
  role: string;
  userId?: string | number;
}

interface Planning {
  id: number;
  cours: string;
  salle: string;
  ecole: string;
  jour: string;
  heureDebut: string;
  heureFin: string;
}

interface TimeSlot {
  time: string;
  startHour: number;
}

interface Day {
  name: string;
  date: Date;
}

interface Disponibilite {
  date: string; // Exact date in YYYY-MM-DD format
  jour: string; // Day name (derived from date)
  heureDebut: string;
  heureFin: string;
}

interface EdtSlot {
  jour: string;
  heureDebut: string;
  heureFin: string;
  ue: string;
  classe: string;
  salle: string;
  ecole: string;
  type: string;
  dateKey?: string; // Clé avec date complète pour filtrage par semaine
}

interface TeacherCourseCard {
  id?: number;
  ueId: number;
  codeUE?: string;
  nomUE?: string;
  idEcole?: string;
  schoolName: string;
  department: string;
  specialty: string;
  teachingModes: string[];
  attendanceRate: number;
  enrolledStudents: number;
  totalSessions: number;
  completedSessions: number;
  progressPercent: number;
  nextSession: string;
  room: string;
  canCM: boolean;
  canTD: boolean;
  canTP: boolean;
}

interface TeacherCourseGroup {
  school: string;
  departments: {
    name: string;
    specialties: {
      name: string;
      courses: TeacherCourseCard[];
    }[];
  }[];
}

@Component({
  selector: 'app-enseignant-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, GestionDisponibilitesComponent, TeacherAttendanceComponent],
  templateUrl: './enseignant-dashboard.component.html',
  styleUrls: ['./enseignant-dashboard.component.css']
})
export class EnseignantDashboardComponent implements OnInit, OnDestroy {
  userInfo: UserInfo | null = null;
  plannings: Planning[] = [];
  totalHeures: number = 0;
  activeSection: string = 'overview';
  
  // Disponibilités
  disponibilites: Disponibilite[] = [];
  nouvelleDispo: Disponibilite = {
    date: '',
    jour: '',
    heureDebut: '',
    heureFin: ''
  };
  joursSemaine: string[] = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  isLoadingDisponibilites: boolean = false;
  isSendingDisponibilites: boolean = false;
  justAdded: boolean = false;
  
  // Business logic validation
  timeValidationMessage: string = '';
  isTimeRangeValid: boolean = true;

  // Method to update day name when date changes
  onDateChange(): void {
    if (this.nouvelleDispo.date) {
      const selectedDate = new Date(this.nouvelleDispo.date);
      const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
      this.nouvelleDispo.jour = dayNames[selectedDate.getDay()];
      
      // Validate the selected date
      this.validateTimeRange();
    }
  }

  // Business Logic Validation Methods
  
  /**
   * Get minimum date (today) for date input
   */
  getMinDate(): string {
    const today = new Date();
    return today.toISOString().split('T')[0];
  }

  /**
   * Check if selected date is in the past
   */
  isPastDate(dateString: string): boolean {
    if (!dateString) return false;
    
    const selectedDate = new Date(dateString);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    selectedDate.setHours(0, 0, 0, 0);
    
    return selectedDate < today;
  }

  /**
   * Check if day is valid (Monday to Saturday only)
   */
  isValidDay(jour: string): boolean {
    if (!jour) return false;
    return jour !== 'Sunday';
  }

  /**
   * Validate time range (8:00 AM to 5:00 PM, start before end)
   */
  validateTimeRange(): void {
    this.timeValidationMessage = '';
    this.isTimeRangeValid = true;

    if (!this.nouvelleDispo.heureDebut || !this.nouvelleDispo.heureFin) {
      return;
    }

    const startTime = this.nouvelleDispo.heureDebut;
    const endTime = this.nouvelleDispo.heureFin;

    // Check business hours (8:00 AM to 5:00 PM)
    if (!this.isWithinBusinessHours(startTime) || !this.isWithinBusinessHours(endTime)) {
      this.timeValidationMessage = '⚠️ Les horaires doivent être entre 8h00 et 17h00';
      this.isTimeRangeValid = false;
      return;
    }

    // Check if start time is before end time
    if (startTime >= endTime) {
      this.timeValidationMessage = '⚠️ L\'heure de début doit être avant l\'heure de fin';
      this.isTimeRangeValid = false;
      return;
    }

    // Check minimum duration (at least 1 hour)
    const startMinutes = this.timeToMinutes(startTime);
    const endMinutes = this.timeToMinutes(endTime);
    const durationMinutes = endMinutes - startMinutes;

    if (durationMinutes < 60) {
      this.timeValidationMessage = '⚠️ La durée minimale est de 1 heure';
      this.isTimeRangeValid = false;
      return;
    }

    // All validations passed
    this.timeValidationMessage = '✅ Créneau valide';
    this.isTimeRangeValid = true;
  }

  /**
   * Check if time is within business hours (8:00 AM to 5:00 PM)
   */
  isWithinBusinessHours(time: string): boolean {
    if (!time) return false;
    
    const timeMinutes = this.timeToMinutes(time);
    const businessStart = 8 * 60; // 8:00 AM in minutes
    const businessEnd = 17 * 60;  // 5:00 PM in minutes
    
    return timeMinutes >= businessStart && timeMinutes <= businessEnd;
  }

  /**
   * Check if the entire form is valid
   */
  isFormValid(): boolean {
    return (
      !!this.nouvelleDispo.date &&
      !this.isPastDate(this.nouvelleDispo.date) &&
      this.isValidDay(this.nouvelleDispo.jour) &&
      !!this.nouvelleDispo.heureDebut &&
      !!this.nouvelleDispo.heureFin &&
      this.isTimeRangeValid
    );
  }

  // EDT
  weekDays: Day[] = [];
  timeSlots: TimeSlot[] = [];
  currentWeekStart: Date = new Date();
  edtMap: Map<string, EdtSlot> = new Map();
  hasEdt: boolean = false;
  isLoadingEdt: boolean = false;
  private ecolesById = new Map<string, string>();
  private lastTeacherEdtData: EmploiDuTempsDTO[] = [];
  allTeacherEdtData: EmploiDuTempsDTO[] = [];
  assignedCourses: UEEnseignantDTO[] = [];
  teacherCourseGroups: TeacherCourseGroup[] = [];
  selectedCourseDetail: TeacherCourseCard | null = null;
  courseDetailTab: 'overview' | 'students' | 'sessions' = 'overview';
  selectedSchoolFilter = 'all';
  selectedDepartmentFilter = 'all';
  selectedSpecialtyFilter = 'all';
  allTeacherCourses: TeacherCourseCard[] = [];
  isLoadingCourses = false;
  overviewInsight: { title: string; subtitle: string; badge: string; details: string[] } | null = null;
  teacherNotifications: TeacherNotification[] = [];
  isLoadingNotifications = false;
  private notificationRefreshHandle?: number;

  private attendanceSessionMap = new Map<string, { attended: number; planned: number; attendanceRate: number; students: any[]; sessions: any[] }>();

  liveCourseRoster: Record<number, any[]> = {};
  liveCourseSessions: Record<number, any[]> = {};
  liveCourseReports: Record<number, { totalSessions: number; totalEnrolledStudents: number; overallAttendanceRate: number }> = {};

  private readonly fallbackCourseNames = ['Software Engineering', 'Database Design', 'Operating Systems', 'Networks', 'Algorithms'];

  constructor(
    private authService: AuthService,
    private planningService: PlanningService,
    private emploiDuTempsService: EmploiDuTempsService,
    private notificationApi: NotificationApiService,
    private notification: NotificationService,
    private ecoleService: EcoleService,
    private ueService: UEService,
    private courseService: CourseService,
    private attendanceService: AttendanceService
  ) {
    this.initTimeSlots();
  }

  ngOnInit(): void {
    // Clear any cached data on init
    this.clearCachedData();
    
    this.userInfo = this.authService.getCurrentUser();
    this.setWeekToCurrent();
    this.loadEdt();
    this.loadEcoles();
    this.loadAssignedCourses();
    this.loadTeacherNotifications();
    this.notificationRefreshHandle = window.setInterval(() => this.loadTeacherNotifications(), 60000);
    this.loadExistingDisponibilites(); // Load existing availabilities

    // Initialize empty plannings - no demo data
    this.plannings = [];

    this.calculerTotalHeures();
  }

  ngOnDestroy(): void {
    if (this.notificationRefreshHandle !== undefined) window.clearInterval(this.notificationRefreshHandle);
  }

  loadTeacherNotifications(): void {
    const user = this.authService.getCurrentUser();
    if (!user?.userId) return;
    this.isLoadingNotifications = true;
    this.notificationApi.getForTeacher(String(user.userId)).subscribe({
      next: notifications => {
        this.teacherNotifications = notifications;
        this.isLoadingNotifications = false;
      },
      error: error => {
        console.error('Erreur de chargement des notifications', error);
        this.isLoadingNotifications = false;
      }
    });
  }

  markTeacherNotificationRead(notification: TeacherNotification): void {
    if (notification.statut === 'LU') return;
    this.notificationApi.markRead(notification.id).subscribe({
      next: updated => {
        this.teacherNotifications = this.teacherNotifications.map(item => item.id === updated.id ? updated : item);
      },
      error: error => console.error('Erreur de mise à jour de notification', error)
    });
  }

  markAllTeacherNotificationsRead(): void {
    const user = this.authService.getCurrentUser();
    if (!user?.userId) return;
    this.notificationApi.markAllRead(String(user.userId)).subscribe({
      next: () => this.loadTeacherNotifications(),
      error: error => console.error('Erreur de mise à jour des notifications', error)
    });
  }

  private clearCachedData(): void {
    // Clear any potential cached disponibilites
    this.disponibilites = [];
    
    // Clear local storage items that might contain cached data
    const keysToRemove = Object.keys(localStorage).filter(key => 
      key.includes('disponibilite') || key.includes('planning') || key.includes('cache')
    );
    
    keysToRemove.forEach(key => {
      console.log('Clearing cached data:', key);
      localStorage.removeItem(key);
    });
  }

  debugClearAll(): void {
    console.log('=== DEBUG CLEAR ALL ===');
    console.log('Before clear - disponibilites:', this.disponibilites);
    
    // Clear all local data
    this.disponibilites = [];
    this.plannings = [];
    
    // Clear all localStorage
    localStorage.clear();
    
    // Clear session storage too
    sessionStorage.clear();
    
    console.log('After clear - disponibilites:', this.disponibilites);
    console.log('LocalStorage cleared, SessionStorage cleared');
    
    this.notification.success('Données et cache effacés. Actualisez la page pour voir l\'état initial.');
  }

  initTimeSlots(): void {
    this.timeSlots = [8, 10, 12, 14, 16].map(hour => ({
      time: `${hour}h - ${hour + 2}h`,
      startHour: hour
    }));
  }

  setWeekToCurrent(): void {
    const now = new Date();
    const day = now.getDay();
    const diff = now.getDate() - day + (day === 0 ? -6 : 1);
    this.currentWeekStart = new Date(now.setDate(diff));
    this.currentWeekStart.setHours(0, 0, 0, 0);
    this.updateWeekDays();
    if (this.allTeacherEdtData && this.allTeacherEdtData.length > 0) {
      this.processEdtDataTeacher(this.allTeacherEdtData);
    } else {
      this.loadEdt();
    }
  }

  changeWeek(offset: number): void {
    const newDate = new Date(this.currentWeekStart);
    newDate.setDate(newDate.getDate() + (offset * 7));
    this.currentWeekStart = newDate;
    this.updateWeekDays();
    if (this.allTeacherEdtData && this.allTeacherEdtData.length > 0) {
      this.processEdtDataTeacher(this.allTeacherEdtData);
    } else {
      this.loadEdt();
    }
  }

  updateWeekDays(): void {
    this.weekDays = [];
    const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    for (let i = 0; i < 6; i++) {
      const date = new Date(this.currentWeekStart);
      date.setDate(date.getDate() + i);
      this.weekDays.push({ name: days[i], date: date });
    }
  }

  private loadEcoles(): void {
    this.ecoleService.obtenirToutes().subscribe({
      next: ecoles => {
        this.ecolesById = new Map(
          ecoles
            .filter(ecole => !!ecole.idEcole)
            .map(ecole => [ecole.idEcole!, ecole.nomEcole] as const)
        );
        if (this.lastTeacherEdtData.length) {
          this.processEdtDataTeacher(this.lastTeacherEdtData);
        }
      },
      error: error => console.error('Erreur lors du chargement des écoles:', error)
    });
  }

  loadEdt(): void {
    const user = this.authService.getCurrentUser();
    this.edtMap.clear();
    this.hasEdt = false;
    
    if (!user) {
      console.warn('Utilisateur non connecté');
      return;
    }

    const rawUserId = (user as any).userId || (user as any).idUser || (user as any).id;
    if (!rawUserId) {
      console.warn('Utilisateur sans ID');
      return;
    }

    const userId = String(rawUserId);
    this.isLoadingEdt = true;

    this.planningService.getEdtFromUserService(userId).subscribe({
      next: (allData: EmploiDuTempsDTO[]) => {
        this.allTeacherEdtData = allData || [];
        this.populatePlanningsList(this.allTeacherEdtData);

        if (this.allTeacherEdtData.length > 0) {
          const weekStartStr = this.formatDate(this.currentWeekStart);
          const weekEnd = new Date(this.currentWeekStart);
          weekEnd.setDate(weekEnd.getDate() + 6);
          const weekEndStr = this.formatDate(weekEnd);

          const hasCourseInCurrentWeek = this.allTeacherEdtData.some(edt => {
            const d = edt.dateJour ? String(edt.dateJour) : (edt.dateHeureDebut ? this.formatDate(new Date(edt.dateHeureDebut)) : '');
            return d >= weekStartStr && d <= weekEndStr;
          });

          if (!hasCourseInCurrentWeek) {
            const firstWithDate = this.allTeacherEdtData.find(edt => edt.dateJour || edt.dateHeureDebut);
            if (firstWithDate) {
              const dateStr = firstWithDate.dateJour ? String(firstWithDate.dateJour) : String(firstWithDate.dateHeureDebut);
              const firstDate = new Date(dateStr);
              if (!isNaN(firstDate.getTime())) {
                const day = firstDate.getDay();
                const diff = firstDate.getDate() - day + (day === 0 ? -6 : 1);
                this.currentWeekStart = new Date(firstDate.setDate(diff));
                this.currentWeekStart.setHours(0, 0, 0, 0);
                this.updateWeekDays();
              }
            }
          }
        }

        this.processEdtDataTeacher(this.allTeacherEdtData);
      },
      error: (err) => {
        console.warn('Fallback vers getEdtByEnseignantAndWeek:', err);
        const dateDebut = this.formatDate(this.currentWeekStart);
        const weekEnd = new Date(this.currentWeekStart);
        weekEnd.setDate(weekEnd.getDate() + 6);
        const dateFin = this.formatDate(weekEnd);
        this.planningService.getEdtByEnseignantAndWeek(userId, dateDebut, dateFin).subscribe({
          next: (data: EmploiDuTempsDTO[]) => {
            this.allTeacherEdtData = data || [];
            this.populatePlanningsList(this.allTeacherEdtData);
            this.processEdtDataTeacher(this.allTeacherEdtData);
          },
          error: (error) => {
            console.error('Erreur chargement EDT:', error);
            this.hasEdt = false;
            this.isLoadingEdt = false;
          }
        });
      }
    });
  }

  private formatHourForDisplay(hourNumber: number | null): string {
    if (hourNumber == null) return '08:00 AM';
    const date = new Date();
    date.setHours(hourNumber, 0, 0, 0);
    return date.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit', hour12: true });
  }

  private formatHourRange(startHour: number | null, endHour: number | null): string {
    return `${this.formatHourForDisplay(startHour)} - ${this.formatHourForDisplay(endHour)}`;
  }

  private formatDayName(dateValue: string | Date): string {
    const d = new Date(dateValue);
    if (isNaN(d.getTime())) return 'Monday';
    const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    return days[d.getDay()];
  }

  private populatePlanningsList(edtData: EmploiDuTempsDTO[]): void {
    this.plannings = (edtData || [])
      .filter(edt => edt.typeCours !== 'TPE')
      .map((edt, idx) => {
        let dateJour = edt.dateJour ? String(edt.dateJour) : '';
        let heureDebutNum: number | null = null;
        let heureFinNum: number | null = null;
        if (edt.dateHeureDebut) {
          const d = new Date(edt.dateHeureDebut);
          if (!isNaN(d.getTime())) {
            if (!dateJour) dateJour = this.formatDate(d);
            heureDebutNum = d.getHours();
          }
        }
        if (edt.dateHeureFin) {
          const d = new Date(edt.dateHeureFin);
          if (!isNaN(d.getTime())) {
            heureFinNum = d.getHours();
          }
        }
        if (heureDebutNum === null && edt.heureDebut != null) {
          heureDebutNum = parseInt(String(edt.heureDebut).replace(/\D+/g, ''), 10);
        }
        if (heureFinNum === null && edt.heureFin != null) {
          heureFinNum = parseInt(String(edt.heureFin).replace(/\D+/g, ''), 10);
        }
        if (heureFinNum == null && heureDebutNum != null) heureFinNum = heureDebutNum + 2;

        const ueLabel = edt.codeUE ? `${edt.codeUE}${edt.nomUE ? ' - ' + edt.nomUE : ''}` : (edt.ue ?? edt.typeCours ?? 'Course');
        const classeLabel = edt.nomClasse ?? edt.classe ?? edt.classeId ?? 'Class';
        const salleLabel = edt.nomSalle ?? edt.salle ?? 'To be defined';
        const ecoleLabel = edt.idEcole ? (this.ecolesById.get(edt.idEcole) ?? edt.idEcole) : 'School not specified';

        let jourLabel = edt.jour || '';
        if (!jourLabel && dateJour) {
          const d = new Date(dateJour + 'T12:00:00');
          if (!isNaN(d.getTime())) {
            jourLabel = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'][d.getDay()];
          }
        }

        return {
          id: edt.id ?? idx,
          cours: `${ueLabel} (${edt.typeCours || 'CM'}) - ${classeLabel}`,
          salle: salleLabel,
          ecole: ecoleLabel,
          jour: jourLabel,
          heureDebut: this.formatHourForDisplay(heureDebutNum),
          heureFin: this.formatHourForDisplay(heureFinNum)
        };
      });
  }

  private processEdtDataTeacher(edtData: EmploiDuTempsDTO[]): void {
    this.lastTeacherEdtData = edtData;
    this.edtMap.clear();

    const weekStartStr = this.formatDate(this.currentWeekStart);
    const weekEnd = new Date(this.currentWeekStart);
    weekEnd.setDate(weekEnd.getDate() + 6);
    const weekEndStr = this.formatDate(weekEnd);

    let matchingCount = 0;

    edtData.forEach(edt => {
      let dateJour = edt.dateJour ? String(edt.dateJour) : '';
      let heureDebutNum: number | null = null;
      let heureFinNum: number | null = null;

      if (edt.dateHeureDebut) {
        const d = new Date(edt.dateHeureDebut);
        if (!isNaN(d.getTime())) {
          if (!dateJour) dateJour = this.formatDate(d);
          heureDebutNum = d.getHours();
        }
      }
      if (edt.dateHeureFin) {
        const d = new Date(edt.dateHeureFin);
        if (!isNaN(d.getTime())) {
          heureFinNum = d.getHours();
        }
      }

      if (heureDebutNum === null && edt.heureDebut != null) {
        heureDebutNum = parseInt(String(edt.heureDebut).replace(/\D+/g, ''), 10);
      }
      if (heureFinNum === null && edt.heureFin != null) {
        heureFinNum = parseInt(String(edt.heureFin).replace(/\D+/g, ''), 10);
      }

      if (!dateJour || heureDebutNum == null) return;
      if (heureFinNum == null) heureFinNum = heureDebutNum + 2;

      // Filter by current week
      if (dateJour < weekStartStr || dateJour > weekEndStr) return;

      matchingCount++;

      const ueLabel = edt.codeUE
        ? `${edt.codeUE}${edt.nomUE ? ' - ' + edt.nomUE : ''}`
        : edt.ue ?? edt.typeCours ?? 'Course';
      const classeLabel = edt.nomClasse ?? edt.classe ?? edt.classeId ?? 'Class';
      const salleLabel = edt.nomSalle ?? edt.salle ?? 'To be defined';
      const ecoleLabel = edt.idEcole
        ? this.ecolesById.get(edt.idEcole) ?? edt.idEcole
        : 'School not specified';

      let jourLabel = edt.jour || '';
      if (!jourLabel && dateJour) {
        const d = new Date(dateJour + 'T12:00:00');
        if (!isNaN(d.getTime())) {
          jourLabel = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'][d.getDay()];
        }
      }

      const dateKey = `${dateJour}_${heureDebutNum}h`;
      const slot: EdtSlot = {
        jour: jourLabel,
        heureDebut: this.formatHourForDisplay(heureDebutNum),
        heureFin: this.formatHourForDisplay(heureFinNum),
        ue: ueLabel,
        classe: classeLabel,
        salle: salleLabel,
        ecole: ecoleLabel,
        type: edt.typeCours || 'CM',
        dateKey
      };

      this.edtMap.set(dateKey, slot);
      if (jourLabel) {
        this.edtMap.set(`${jourLabel}_${heureDebutNum}h`, slot);
      }
    });

    this.hasEdt = matchingCount > 0;
    this.calculerTotalHeures();
    this.isLoadingEdt = false;
  }

  getSectionTitle(): string {
    const titles: { [key: string]: string } = {
      'overview': 'Teacher Dashboard',
      'planning': 'My Planning',
      'disponibilites': 'My Availabilities',
      'courses': 'My Courses',
      'reservations': 'Room Reservations',
      'students': 'My Students',
      'attendance': 'QR Attendance',
      'notifications': 'Notifications'
    };
    return titles[this.activeSection] || 'Teacher Dashboard';
  }

  calculerTotalHeures(): void {
    this.totalHeures = this.plannings.reduce((total, planning) => {
      const debut = this.convertirHeureEnMinutes(planning.heureDebut);
      const fin = this.convertirHeureEnMinutes(planning.heureFin);
      return total + (fin - debut) / 60;
    }, 0);
  }

  convertirHeureEnMinutes(heure: string): number {
    const [hourText, minuteText = '0'] = heure.replace('h', '').split(':');
    return Number(hourText) * 60 + Number(minuteText);
  }

  logout(): void {
    this.authService.logout();
  }

  // Gestion des disponibilités
  ajouterDisponibilite(): void {
    // Use comprehensive form validation
    if (!this.isFormValid()) {
      this.notification.warning('Veuillez corriger les erreurs de validation avant d\'ajouter ce créneau.');
      return;
    }
    
    // Additional business logic checks
    if (this.isPastDate(this.nouvelleDispo.date)) {
      this.notification.warning('Vous ne pouvez pas sélectionner une date dans le passé.');
      return;
    }
    
    if (!this.isValidDay(this.nouvelleDispo.jour)) {
      this.notification.warning('Les disponibilités ne peuvent être ajoutées que du lundi au samedi.');
      return;
    }
    
    if (!this.isWithinBusinessHours(this.nouvelleDispo.heureDebut) || 
        !this.isWithinBusinessHours(this.nouvelleDispo.heureFin)) {
      this.notification.warning('Les horaires doivent être entre 8h00 et 17h00 (heures ouvrables).');
      return;
    }
    
    // Check for time conflicts on the same date
    const conflict = this.disponibilites.find(dispo => 
      dispo.date === this.nouvelleDispo.date &&
      this.hasTimeOverlap(dispo, this.nouvelleDispo)
    );
    
    if (conflict) {
      this.notification.warning(`Conflit détecté avec le créneau ${conflict.heureDebut} - ${conflict.heureFin} du ${conflict.date}`);
      return;
    }
    
    // All validations passed - add the availability
    this.disponibilites.push({ ...this.nouvelleDispo });
    this.justAdded = true;
    
    // Reset form for next entry
    this.nouvelleDispo.date = '';
    this.nouvelleDispo.jour = '';
    this.nouvelleDispo.heureDebut = '';
    this.nouvelleDispo.heureFin = '';
    this.timeValidationMessage = '';
    this.isTimeRangeValid = true;
    
    // Remove highlight after animation
    setTimeout(() => this.justAdded = false, 2000);
  }

  hasTimeOverlap(dispo1: Disponibilite, dispo2: Disponibilite): boolean {
    const start1 = this.timeToMinutes(dispo1.heureDebut);
    const end1 = this.timeToMinutes(dispo1.heureFin);
    const start2 = this.timeToMinutes(dispo2.heureDebut);
    const end2 = this.timeToMinutes(dispo2.heureFin);
    
    return start1 < end2 && start2 < end1;
  }

  timeToMinutes(time: string): number {
    const [hours, minutes] = time.split(':').map(Number);
    return hours * 60 + minutes;
  }

  calculateDuration(heureDebut: string, heureFin: string): string {
    const start = this.timeToMinutes(heureDebut);
    const end = this.timeToMinutes(heureFin);
    const duration = end - start;
    
    const hours = Math.floor(duration / 60);
    const minutes = duration % 60;
    
    if (hours === 0) return `${minutes}min`;
    if (minutes === 0) return `${hours}h`;
    return `${hours}h${minutes}min`;
  }

  getTotalHoursAvailable(): number {
    let totalMinutes = 0;
    this.disponibilites.forEach(dispo => {
      const start = this.timeToMinutes(dispo.heureDebut);
      const end = this.timeToMinutes(dispo.heureFin);
      totalMinutes += (end - start);
    });
    return Math.round((totalMinutes / 60) * 10) / 10;
  }

  getDisponibilitesForDay(jour: string): Disponibilite[] {
    return this.disponibilites.filter(dispo => dispo.jour === jour);
  }

  clearAllDisponibilites(): void {
    this.notification.confirm('Êtes-vous sûr de vouloir supprimer toutes vos disponibilités ?', 'Confirmation', () => {
      this.disponibilites = [];
      const user = this.authService.getCurrentUser();
      if (user && (user as any).userId) {
        console.log('Disponibilités locales effacées. Pour supprimer du serveur, implémentez l\'API correspondante.');
      }
    });
  }

  supprimerDisponibilite(index: number): void {
    this.disponibilites.splice(index, 1);
  }

  envoyerDisponibilites(): void {
    const user = this.authService.getCurrentUser();
    if (!user || !(user as any).userId) {
      this.notification.error('Utilisateur non connecté');
      return;
    }

    if (this.disponibilites.length === 0) {
      this.notification.warning('Aucune disponibilité à envoyer');
      return;
    }

    this.isSendingDisponibilites = true;
    const userId = (user as any).userId;
    let successCount = 0;
    let errorCount = 0;
    const totalDispos = this.disponibilites.length;

    console.log('Envoi des disponibilités à l\'administrateur:', this.disponibilites);

    // Envoyer chaque disponibilité individuellement
    this.disponibilites.forEach((dispo, index) => {
      const dispoDTO = this.convertToDisponibiliteDTO(dispo, userId);
      
      this.planningService.addDisponibilite(dispoDTO).subscribe({
        next: (response) => {
          successCount++;
          console.log(`Disponibilité ${index + 1} envoyée avec succès:`, response);
          
          // Vérifier si toutes les disponibilités ont été traitées
          if (successCount + errorCount === totalDispos) {
            this.handleSendComplete(successCount, errorCount);
          }
        },
        error: (error) => {
          errorCount++;
          console.error(`Erreur lors de l'envoi de la disponibilité ${index + 1}:`, error);
          
          // Vérifier si toutes les disponibilités ont été traitées
          if (successCount + errorCount === totalDispos) {
            this.handleSendComplete(successCount, errorCount);
          }
        }
      });
    });
  }

  private convertToDisponibiliteDTO(dispo: Disponibilite, enseignantId: string): DisponibiliteDTO {
    // Use the exact date selected by the teacher
    const selectedDate = new Date(dispo.date);
    const [heureDebut, minuteDebut] = dispo.heureDebut.split(':').map(Number);
    const [heureFin, minuteFin] = dispo.heureFin.split(':').map(Number);

    // Create start datetime with selected date and start time
    const dateHeureDebut = new Date(selectedDate);
    dateHeureDebut.setHours(heureDebut, minuteDebut, 0, 0);

    // Create end datetime with selected date and end time
    const dateHeureFin = new Date(selectedDate);
    dateHeureFin.setHours(heureFin, minuteFin, 0, 0);

    // Format as local datetime string to avoid timezone conversion
    const formatLocalDateTime = (date: Date): string => {
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      const seconds = date.getSeconds().toString().padStart(2, '0');
      return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
    };

    return {
      enseignantId: enseignantId,
      idEcole: (this.userInfo as any)?.idEcole || (this.userInfo as any)?.idEcoles?.[0] || '',
      dateHeureDebut: formatLocalDateTime(dateHeureDebut),
      dateHeureFin: formatLocalDateTime(dateHeureFin),
      commentaire: `Disponibilité pour le ${dispo.jour} ${dispo.date}`
    };
  }

  private handleSendComplete(successCount: number, errorCount: number): void {
    this.isSendingDisponibilites = false;
    
    if (errorCount === 0) {
      this.notification.success(`Toutes vos disponibilités (${successCount}) ont été envoyées à l'administrateur avec succès !`);
      this.disponibilites = []; // Reset après envoi réussi
    } else if (successCount > 0) {
      this.notification.info(`${successCount} disponibilités envoyées avec succès, ${errorCount} ont échoué. Veuillez réessayer pour les échecs.`);
    } else {
      this.notification.error(`Aucune disponibilité n'a pu être envoyée. Vérifiez votre connexion et réessayez.`);
    }
  }

  loadExistingDisponibilites(): void {
    const user = this.authService.getCurrentUser();
    if (!user || !(user as any).userId) {
      console.log('No user logged in, skipping disponibilites load');
      return;
    }

    const userId = (user as any).userId;
    console.log('Loading disponibilites for user ID:', userId);
    
    this.planningService.getDisponibilites(userId).subscribe({
      next: (disponibilites: DisponibiliteDTO[]) => {
        console.log('Raw disponibilites from server:', disponibilites);
        // Convertir les DisponibiliteDTO en format local pour affichage
        this.disponibilites = disponibilites.map(dto => this.convertFromDisponibiliteDTO(dto));
        console.log('Converted disponibilites for display:', this.disponibilites);
        console.log('Total disponibilites loaded:', this.disponibilites.length);
      },
      error: (error) => {
        console.error('Erreur lors du chargement des disponibilités existantes:', error);
        console.log('Setting disponibilites to empty array due to error');
        this.disponibilites = []; // Ensure empty on error
        // Ne pas afficher d'erreur à l'utilisateur car ce n'est pas critique
      }
    });
  }

  private convertFromDisponibiliteDTO(dto: DisponibiliteDTO): Disponibilite {
    const dateDebut = new Date(dto.dateHeureDebut);
    const dateFin = new Date(dto.dateHeureFin);
    
    const jourIndex = dateDebut.getDay();
    const jours = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const dateStr = dateDebut.toISOString().split('T')[0];
    
    return {
      date: dateStr,
      jour: jours[jourIndex],
      heureDebut: dateDebut.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true }),
      heureFin: dateFin.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: true })
    };
  }

  loadAssignedCourses(): void {
    const user = this.authService.getCurrentUser();
    if (!user || !(user as any).userId) {
      this.assignedCourses = [];
      this.teacherCourseGroups = [];
      this.allTeacherCourses = [];
      return;
    }

    this.isLoadingCourses = true;
    const userId = String((user as any).userId);

    this.emploiDuTempsService.getUEEnseignantsParEnseignant(userId).subscribe({
      next: (data: UEEnseignantDTO[]) => {
        this.assignedCourses = data || [];
        this.buildTeacherCourseCards();
        this.loadTeacherCourseDetailData();
        this.isLoadingCourses = false;
      },
      error: () => {
        this.assignedCourses = [];
        this.teacherCourseGroups = [];
        this.allTeacherCourses = [];
        this.isLoadingCourses = false;
      }
    });
  }

  private buildTeacherCourseCards(): void {
    const user = this.authService.getCurrentUser();
    const teacherId = user && (user as any).userId ? String((user as any).userId) : '';

    if (!teacherId || !this.assignedCourses || this.assignedCourses.length === 0) {
      this.allTeacherCourses = [];
      this.teacherCourseGroups = [];
      return;
    }

    const ueMap = new Map<number, UEDTO>();
    this.ueService.getAllUEs().subscribe({
      next: (ues: UEDTO[]) => {
        ues.forEach(ue => { if (ue.id != null) ueMap.set(ue.id, ue); });
        const schoolMap = new Map<string, string>(Object.entries(this.ecolesById).map(([key, value]) => [key, value]));

        forkJoin({
          courses: this.courseService.getCoursesByEnseignant(teacherId),
          attendance: this.attendanceService.getActiveSessionsHttp(teacherId)
        }).subscribe({
          next: ({ courses, attendance }) => {
            const activeCourseIds = new Set(attendance.map(session => String(session.courseId)));

            this.allTeacherCourses = (this.assignedCourses || []).map((entry, index) => {
              const ue = ueMap.get(entry.ueId);
              const schoolName = schoolMap.get(String(entry.idEcole)) || 'School not specified';
              const department = ue?.nomFiliere || ue?.description || 'Department';
              const specialty = ue?.specialite || 'General curriculum';
              const assignedCourse = courses.find(course => Number(course.ueId) === Number(entry.ueId)) || courses[index] || null;
              const courseId = (assignedCourse?.id ?? Number(entry.ueId)) || index + 1;
              const isActiveCourse = activeCourseIds.has(String(courseId));
              const sessionCount = assignedCourse ? Math.max(4, (assignedCourse.nombreHeures ?? 0) > 0 ? Math.ceil((assignedCourse.nombreHeures ?? 0) / 2) : 6) : 8;

              const liveReport = this.liveCourseReports[courseId] ?? {
                totalSessions: sessionCount,
                totalEnrolledStudents: 0,
                overallAttendanceRate: 0
              };

              const roster = this.liveCourseRoster[courseId] ?? [];
              const sessionSummary = this.attendanceSessionMap.get(String(courseId)) ?? {
                attended: Math.min(Math.max(0, Math.round((liveReport.overallAttendanceRate / 100) * sessionCount)), sessionCount),
                planned: sessionCount,
                attendanceRate: liveReport.overallAttendanceRate || 0,
                students: roster,
                sessions: this.liveCourseSessions[courseId] ?? []
              };

              const enforcedStudents = roster.length > 0 ? roster.length : Math.max(1, (liveReport.totalEnrolledStudents || sessionCount * 2));
              const attendanceRate = sessionSummary.attendanceRate || Math.min(100, Math.max(0, Math.round(((sessionSummary.attended || 0) / Math.max(sessionSummary.planned || 1, 1)) * 100)) || 0);
              const completedSessions = Math.min(sessionSummary.planned || sessionCount, sessionSummary.attended || Math.max(0, Math.round((attendanceRate / 100) * sessionCount)));
              const totalSessions = sessionSummary.planned || sessionCount;
              const nextSessionText = isActiveCourse ? 'Active session now' : (this.fallbackCourseNames[index % this.fallbackCourseNames.length] || 'Next session');

              return {
                id: entry.id ?? courseId,
                ueId: entry.ueId,
                codeUE: entry.codeUE || ue?.codeUE || `UE-${entry.ueId}`,
                nomUE: entry.nomUE || ue?.nomUE || assignedCourse?.ueNom || 'Teaching Unit',
                idEcole: entry.idEcole,
                schoolName,
                department,
                specialty,
                teachingModes: [
                  entry.peutCM ? 'Lecture (CM)' : null,
                  entry.peutTD ? 'Tutorial (TD)' : null,
                  entry.peutTP ? 'Practical Lab (TP)' : null
                ].filter(Boolean) as string[],
                attendanceRate,
                enrolledStudents: enforcedStudents,
                totalSessions,
                completedSessions,
                progressPercent: Math.min(100, Math.round((completedSessions / Math.max(totalSessions, 1)) * 100)),
                nextSession: nextSessionText,
                room: (sessionSummary.sessions?.[0]?.room) || 'Room TBD',
                canCM: entry.peutCM,
                canTD: entry.peutTD,
                canTP: entry.peutTP
              };
            });

            this.teacherCourseGroups = this.buildCourseGroups(this.allTeacherCourses);
            this.isLoadingCourses = false;
          },
          error: () => {
            this.allTeacherCourses = (this.assignedCourses || []).map((entry, index) => ({
              id: entry.id ?? index + 1,
              ueId: entry.ueId,
              codeUE: entry.codeUE || `UE-${entry.ueId}`,
              nomUE: entry.nomUE || 'Teaching Unit',
              idEcole: entry.idEcole,
              schoolName: this.ecolesById.get(String(entry.idEcole)) || 'School not specified',
              department: 'Department',
              specialty: 'General curriculum',
              teachingModes: [
                entry.peutCM ? 'Lecture (CM)' : null,
                entry.peutTD ? 'Tutorial (TD)' : null,
                entry.peutTP ? 'Practical Lab (TP)' : null
              ].filter(Boolean) as string[],
              attendanceRate: 0,
              enrolledStudents: 0,
              totalSessions: 0,
              completedSessions: 0,
              progressPercent: 0,
              nextSession: 'No session available',
              room: 'Room TBD',
              canCM: entry.peutCM,
              canTD: entry.peutTD,
              canTP: entry.peutTP
            }));
            this.teacherCourseGroups = this.buildCourseGroups(this.allTeacherCourses);
            this.isLoadingCourses = false;
          }
        });
      },
      error: () => {
        this.allTeacherCourses = (this.assignedCourses || []).map((entry, index) => ({
          id: entry.id ?? index + 1,
          ueId: entry.ueId,
          codeUE: entry.codeUE || `UE-${entry.ueId}`,
          nomUE: entry.nomUE || 'Teaching Unit',
          idEcole: entry.idEcole,
          schoolName: this.ecolesById.get(String(entry.idEcole)) || 'School not specified',
          department: 'Department',
          specialty: 'General curriculum',
          teachingModes: [
            entry.peutCM ? 'Lecture (CM)' : null,
            entry.peutTD ? 'Tutorial (TD)' : null,
            entry.peutTP ? 'Practical Lab (TP)' : null
          ].filter(Boolean) as string[],
          attendanceRate: 0,
          enrolledStudents: 0,
          totalSessions: 0,
          completedSessions: 0,
          progressPercent: 0,
          nextSession: 'No session available',
          room: 'Room TBD',
          canCM: entry.peutCM,
          canTD: entry.peutTD,
          canTP: entry.peutTP
        }));
        this.teacherCourseGroups = this.buildCourseGroups(this.allTeacherCourses);
        this.isLoadingCourses = false;
      }
    });
  }

  private buildCourseGroups(courses: TeacherCourseCard[]): TeacherCourseGroup[] {
    const grouped = new Map<string, TeacherCourseGroup>();

    courses.forEach(course => {
      const schoolKey = course.schoolName || 'School not specified';
      if (!grouped.has(schoolKey)) {
        grouped.set(schoolKey, {
          school: schoolKey,
          departments: []
        });
      }

      const schoolGroup = grouped.get(schoolKey)!;
      let departmentEntry = schoolGroup.departments.find(item => item.name === course.department);
      if (!departmentEntry) {
        departmentEntry = { name: course.department, specialties: [] };
        schoolGroup.departments.push(departmentEntry);
      }

      let specialtyEntry = departmentEntry.specialties.find(item => item.name === course.specialty);
      if (!specialtyEntry) {
        specialtyEntry = { name: course.specialty, courses: [] };
        departmentEntry.specialties.push(specialtyEntry);
      }

      specialtyEntry.courses.push(course);
    });

    return Array.from(grouped.values());
  }

  get filteredTeacherCourseGroups(): TeacherCourseGroup[] {
    const groups = this.teacherCourseGroups || [];
    const schoolSelected = this.selectedSchoolFilter;
    const departmentSelected = this.selectedDepartmentFilter;
    const specialtySelected = this.selectedSpecialtyFilter;

    return groups
      .filter(group => schoolSelected === 'all' || group.school === schoolSelected)
      .map(group => ({
        ...group,
        departments: group.departments
          .filter(dept => departmentSelected === 'all' || dept.name === departmentSelected)
          .map(dept => ({
            ...dept,
            specialties: dept.specialties
              .filter(spec => specialtySelected === 'all' || spec.name === specialtySelected)
              .map(spec => ({
                ...spec,
                courses: spec.courses
              }))
              .filter(spec => spec.courses.length > 0)
          }))
          .filter(dept => dept.specialties.length > 0)
      }))
      .filter(group => group.departments.length > 0);
  }

  get teacherCourseSchools(): string[] {
    return [...new Set((this.teacherCourseGroups || []).map(group => group.school))];
  }

  get teacherCourseDepartments(): string[] {
    const groups = this.filteredTeacherCourseGroups.length ? this.filteredTeacherCourseGroups : this.teacherCourseGroups;
    return [...new Set((groups || []).flatMap(group => group.departments.map(dept => dept.name)))];
  }

  get teacherCourseSpecialties(): string[] {
    const groups = this.filteredTeacherCourseGroups.length ? this.filteredTeacherCourseGroups : this.teacherCourseGroups;
    return [...new Set((groups || []).flatMap(group => group.departments.flatMap(dept => dept.specialties.map(spec => spec.name))))];
  }

  openCourseDetail(course: TeacherCourseCard): void {
    this.selectedCourseDetail = course;
    this.courseDetailTab = 'overview';

    const courseId = Number(course.id ?? course.ueId) || Number(course.ueId) || 1;
    this.attendanceService.getCourseStudentsHttp(courseId).subscribe({
      next: (students) => {
        this.liveCourseRoster[courseId] = students;
      }
    });

    this.attendanceService.getCourseSessionsHttp(courseId).subscribe({
      next: (sessions) => {
        this.liveCourseSessions[courseId] = sessions;
        this.attendanceSessionMap.set(String(courseId), {
          attended: Math.max(0, sessions.length),
          planned: Math.max(1, sessions.length || course.totalSessions),
          attendanceRate: course.attendanceRate,
          students: this.liveCourseRoster[courseId] || [],
          sessions
        });
      }
    });

    this.attendanceService.getCourseReportHttp(courseId).subscribe({
      next: (report) => {
        if (report) {
          this.liveCourseReports[courseId] = {
            totalSessions: report.totalSessions ?? course.totalSessions,
            totalEnrolledStudents: report.totalEnrolledStudents ?? course.enrolledStudents,
            overallAttendanceRate: report.overallAttendanceRate ?? course.attendanceRate
          };
        }
      }
    });
  }

  closeCourseDetail(): void {
    this.selectedCourseDetail = null;
  }

  getCourseModeLabel(mode: string): string {
    return mode || 'Lecture';
  }

  getCourseStudentRoster(course: TeacherCourseCard): any[] {
    const courseId = Number(course.id ?? course.ueId) || Number(course.ueId) || 1;
    const roster = this.liveCourseRoster[courseId] ?? [];
    if (roster.length > 0) {
      return roster.map((student, index) => ({
        name: student.studentName || student.name || `Student ${index + 1}`,
        studentId: student.studentId || student.id || `ST-${String(course.ueId).padStart(3, '0')}-${index + 1}`,
        email: student.studentEmail || student.email || 'student@planora.edu',
        attendanceRate: student.attendanceRate ?? student.percentage ?? 0,
        status: student.status || 'Active'
      }));
    }

    return Array.from({ length: Math.min(course.enrolledStudents || 0, 8) }, (_, index) => ({
      name: `Student ${index + 1}`,
      studentId: `ST-${String(course.ueId).padStart(3, '0')}-${index + 1}`,
      email: `student${index + 1}@planora.edu`,
      attendanceRate: 0,
      status: 'Pending'
    }));
  }

  getCourseSessionSummary(course: TeacherCourseCard): { attended: number; planned: number; attendanceRate: number } {
    const courseId = Number(course.id ?? course.ueId) || Number(course.ueId) || 1;
    const summary = this.attendanceSessionMap.get(String(courseId));
    if (summary) {
      return {
        attended: summary.attended,
        planned: summary.planned,
        attendanceRate: summary.attendanceRate
      };
    }

    const planned = course.totalSessions || 1;
    const attended = course.completedSessions || 0;
    const attendanceRate = Math.min(100, Math.round((attended / Math.max(planned, 1)) * 100));
    return { attended, planned, attendanceRate };
  }

  private formatDate(date: Date | string): string {
    const d = new Date(date);
    if (isNaN(d.getTime())) return '';
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  setActiveSection(section: string): void {
    this.activeSection = section;
  }

  getTotalCoursWeek(): number {
    return this.plannings.length;
  }

  getTotalHeuresWeek(): number {
    return this.totalHeures;
  }

  getEdtSlot(day: Day, slot: TimeSlot): EdtSlot | null {
    const literalKey = `${day.name}_${slot.startHour}h`;
    const dateKey = `${this.formatDate(day.date)}_${slot.startHour}h`;
    return this.edtMap.get(literalKey) ?? this.edtMap.get(dateKey) ?? null;
  }

  hasEdtSlot(day: Day, slot: TimeSlot): boolean {
    return this.getEdtSlot(day, slot) !== null;
  }

  isPastDay(day: Day): boolean {
    const today = new Date();
    const date = new Date(day.date);
    today.setHours(0, 0, 0, 0);
    date.setHours(0, 0, 0, 0);
    return date < today;
  }

  isPastSlot(day: Day, slot: TimeSlot): boolean {
    const slotCourse = this.getEdtSlot(day, slot);
    if (!slotCourse) return false;
    const courseDate = new Date(day.date);
    courseDate.setHours(0, 0, 0, 0);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return courseDate < today;
  }

  private loadTeacherCourseDetailData(): void {
    const user = this.authService.getCurrentUser();
    const userId = user && (user as any).userId ? String((user as any).userId) : '';
    if (!userId || !this.assignedCourses.length) return;

    const requests = this.assignedCourses.map((entry) => {
      const courseTargetId = Number(entry.ueId) || Number(entry.id) || 0;
      if (!courseTargetId) return null;

      return forkJoin({
        students: this.attendanceService.getCourseStudentsHttp(courseTargetId),
        sessions: this.attendanceService.getCourseSessionsHttp(courseTargetId),
        report: this.attendanceService.getCourseReportHttp(courseTargetId)
      }).pipe();
    }).filter(Boolean) as any[];

    if (!requests.length) return;

    forkJoin(requests).subscribe({
      next: (results) => {
        results.forEach((result, index) => {
          const entry = this.assignedCourses[index];
          if (!entry) return;
          const courseId = Number(entry.ueId) || Number(entry.id) || index + 1;
          const students = Array.isArray(result?.students) ? result.students : [];
          const sessions = Array.isArray(result?.sessions) ? result.sessions : [];
          const report = result?.report || null;

          this.liveCourseRoster[courseId] = students;
          this.liveCourseSessions[courseId] = sessions;
          this.liveCourseReports[courseId] = {
            totalSessions: report?.totalSessions ?? Math.max(1, sessions.length || 4),
            totalEnrolledStudents: report?.totalEnrolledStudents ?? students.length,
            overallAttendanceRate: report?.overallAttendanceRate ?? 0
          };

          this.attendanceSessionMap.set(String(courseId), {
            attended: Math.min(Math.max(0, sessions.length), Math.max(1, sessions.length || 4)),
            planned: Math.max(1, (report?.totalSessions ?? sessions.length) || 4),
            attendanceRate: report?.overallAttendanceRate ?? 0,
            students,
            sessions
          });
        });

        this.buildTeacherCourseCards();
      }
    });
  }

  getAverageAttendance(): number {
    if (!this.allTeacherCourses || !this.allTeacherCourses.length) return 0;
    const sum = this.allTeacherCourses.reduce((acc, item) => acc + (item.attendanceRate || 0), 0);
    return Math.round(sum / this.allTeacherCourses.length);
  }

  getCompletedSessionsCount(): number {
    if (!this.allTeacherCourses || !this.allTeacherCourses.length) return 0;
    return this.allTeacherCourses.reduce((acc, item) => acc + (item.completedSessions || 0), 0);
  }

  getTotalSessionsCount(): number {
    if (!this.allTeacherCourses || !this.allTeacherCourses.length) return 0;
    return this.allTeacherCourses.reduce((acc, item) => acc + (item.totalSessions || 0), 0);
  }

  getSchoolCourseCount(schoolGroup: any): number {
    if (!schoolGroup || !schoolGroup.departments || !schoolGroup.departments.length) return 0;
    return schoolGroup.departments.reduce((sum: number, dept: any) =>
      sum + ((dept.specialties && dept.specialties.reduce((s: number, spec: any) => s + (spec.courses ? spec.courses.length : 0), 0)) || 0), 0);
  }

}
