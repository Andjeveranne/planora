import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { PlanningService, EmploiDuTempsDTO, DisponibiliteDTO } from '../services/planning.service';
import { EmploiDuTempsService, UEEnseignantDTO } from '../services/emploi-du-temps.service';
import { NotificationService } from '../services/notification.service';
import { NotificationApiService, TeacherNotification } from '../services/notification-api.service';
import { GestionDisponibilitesComponent } from './gestion-disponibilites/gestion-disponibilites.component';

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
  type: string;
  dateKey?: string; // Clé avec date complète pour filtrage par semaine
}

@Component({
  selector: 'app-enseignant-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, GestionDisponibilitesComponent],
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
  joursSemaine: string[] = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
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
      const dayNames = ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
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
    return jour !== 'Dimanche';
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
  assignedCourses: UEEnseignantDTO[] = [];
  isLoadingCourses = false;
  teacherNotifications: TeacherNotification[] = [];
  isLoadingNotifications = false;
  private notificationRefreshHandle?: number;

  constructor(
    private authService: AuthService,
    private planningService: PlanningService,
    private emploiDuTempsService: EmploiDuTempsService,
    private notificationApi: NotificationApiService,
    private notification: NotificationService
  ) {
    this.initTimeSlots();
  }

  ngOnInit(): void {
    // Clear any cached data on init
    this.clearCachedData();
    
    this.userInfo = this.authService.getCurrentUser();
    this.setWeekToCurrent();
    this.loadEdt();
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
  }

  changeWeek(offset: number): void {
    const newDate = new Date(this.currentWeekStart);
    newDate.setDate(newDate.getDate() + (offset * 7));
    this.currentWeekStart = newDate;
    this.updateWeekDays();
    this.loadEdt(); // Recharger l'EDT pour la nouvelle semaine
  }

  updateWeekDays(): void {
    this.weekDays = [];
    const days = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
    for (let i = 0; i < 6; i++) {
      const date = new Date(this.currentWeekStart);
      date.setDate(date.getDate() + i);
      this.weekDays.push({ name: days[i], date: date });
    }
  }

  loadEdt(): void {
    // Charger l'EDT depuis l'API (généré par l'admin)
    const user = this.authService.getCurrentUser();
    this.edtMap.clear();
    this.hasEdt = false;
    
    if (!user || !(user as any).userId) {
      console.warn('Utilisateur non connecté ou sans ID');
      return;
    }
    
    const userId = (user as any).userId;
    this.isLoadingEdt = true;
    
    // Calculer les dates de début et fin de semaine
    const dateDebut = this.formatDate(this.currentWeekStart);
    const weekEnd = new Date(this.currentWeekStart);
    weekEnd.setDate(weekEnd.getDate() + 6);
    const dateFin = this.formatDate(weekEnd);
    
    this.planningService.getEdtByEnseignantAndWeek(userId, dateDebut, dateFin).subscribe({
      next: (edtData: EmploiDuTempsDTO[]) => {
        const weekStart = this.currentWeekStart.getTime();
        const weekEndTime = new Date(dateFin).getTime() + 86400000;
        this.processEdtDataTeacher(edtData.filter(edt => {
          const start = edt.dateHeureDebut ? new Date(edt.dateHeureDebut).getTime() : NaN;
          return Number.isFinite(start) && start >= weekStart && start < weekEndTime;
        }));
      },
      error: (error) => {
        console.error('Erreur chargement EDT:', error);
        this.hasEdt = false;
        this.isLoadingEdt = false;
      }
    });
  }

  private loadEdtFromPlanningServiceTeacher(userId: string, dateDebut: string, dateFin: string): void {
    this.planningService.getEdtByEnseignantAndWeek(userId, dateDebut, dateFin).subscribe({
      next: (edtData: EmploiDuTempsDTO[]) => {
        console.log('EDT chargé depuis PlanningService:', edtData);
        this.processEdtDataTeacher(edtData);
      },
      error: (err) => {
        console.error('Erreur chargement EDT:', err);
        this.hasEdt = false;
        this.isLoadingEdt = false;
      }
    });
  }

  private processEdtDataTeacher(edtData: EmploiDuTempsDTO[]): void {
    this.edtMap.clear();
    this.plannings = [];
    edtData.forEach(edt => {
      const start = edt.dateHeureDebut ? new Date(edt.dateHeureDebut) : null;
      const end = edt.dateHeureFin ? new Date(edt.dateHeureFin) : null;
      const dateJour = start ? this.formatDate(start) : edt.dateJour;
      const heureDebut = start?.getHours() ?? edt.heureDebut;
      const heureFin = end?.getHours() ?? edt.heureFin;
      if (!dateJour || heureDebut == null || heureFin == null) return;
      const dateKey = `${dateJour}_${heureDebut}h`;
      const ueLabel = edt.codeUE
        ? `${edt.codeUE}${edt.nomUE ? ` - ${edt.nomUE}` : ''}`
        : edt.ue ?? edt.typeCours;
      const classeLabel = edt.nomClasse ?? edt.classe ?? edt.classeId ?? 'Classe';
      const salleLabel = edt.nomSalle ?? edt.salle ?? 'À définir';
      const jourLabel = start
        ? ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'][start.getDay()]
        : edt.jour ?? '';
      const slot: EdtSlot = {
        jour: jourLabel,
        heureDebut: `${heureDebut}h`,
        heureFin: `${heureFin}h`,
        ue: ueLabel,
        classe: classeLabel,
        salle: salleLabel,
        type: edt.typeCours,
        dateKey
      };
      this.edtMap.set(dateKey, slot);
      if (edt.typeCours !== 'TPE') {
        this.plannings.push({
          id: edt.id ?? 0,
          cours: `${ueLabel} (${edt.typeCours}) - ${classeLabel}`,
          salle: salleLabel,
          jour: jourLabel,
          heureDebut: `${heureDebut}h`,
          heureFin: `${heureFin}h`
        });
      }
    });
    this.hasEdt = edtData.length > 0;
    this.calculerTotalHeures();
    this.isLoadingEdt = false;
  }

  loadAssignedCourses(): void {
    const user = this.authService.getCurrentUser();
    if (!user?.userId) {
      this.assignedCourses = [];
      return;
    }
    this.isLoadingCourses = true;
    this.emploiDuTempsService.getUEEnseignantsParEnseignant(String(user.userId)).subscribe({
      next: courses => {
        this.assignedCourses = courses;
        this.isLoadingCourses = false;
      },
      error: error => {
        console.error('Erreur lors du chargement des cours assignés:', error);
        this.assignedCourses = [];
        this.isLoadingCourses = false;
      }
    });
  }

  formatDate(date: Date): string {
    const y = date.getFullYear();
    const m = (date.getMonth() + 1).toString().padStart(2, '0');
    const d = date.getDate().toString().padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  getEdtSlot(day: Day, slot: TimeSlot): EdtSlot | null {
    // Créer la clé avec la date complète
    const dateKey = this.getDateKey(day.date, slot.startHour);
    let edtSlot = this.edtMap.get(dateKey);
    
    // Fallback: chercher avec le nom du jour (ancienne méthode)
    if (!edtSlot) {
      const oldKey = `${day.name}_${slot.startHour}h`;
      edtSlot = this.edtMap.get(oldKey);
    }
    
    return edtSlot || null;
  }

  getDateKey(date: Date, hour: number): string {
    const y = date.getFullYear();
    const m = (date.getMonth() + 1).toString().padStart(2, '0');
    const d = date.getDate().toString().padStart(2, '0');
    return `${y}-${m}-${d}_${hour}h`;
  }

  hasEdtSlot(day: Day, slot: TimeSlot): boolean {
    return this.getEdtSlot(day, slot) !== null;
  }

  getTotalCoursWeek(): number {
    let count = 0;
    for (const day of this.weekDays) {
      for (const slot of this.timeSlots) {
        if (this.hasEdtSlot(day, slot)) {
          count++;
        }
      }
    }
    return count;
  }

  getTotalHeuresWeek(): number {
    return this.getTotalCoursWeek() * 2;
  }

  // Vérifie si un jour est passé (avant aujourd'hui)
  isPastDay(day: Day): boolean {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const dayDate = new Date(day.date);
    dayDate.setHours(0, 0, 0, 0);
    return dayDate < today;
  }

  // Vérifie si un créneau spécifique est passé (jour passé OU heure passée aujourd'hui)
  isPastSlot(day: Day, slot: TimeSlot): boolean {
    const now = new Date();
    const slotDate = new Date(day.date);
    slotDate.setHours(slot.startHour, 0, 0, 0);
    return slotDate < now;
  }

  setActiveSection(section: string): void {
    this.activeSection = section;
    if (section === 'planning') {
      this.loadEdt();
    }
  }

  getSectionTitle(): string {
    const titles: { [key: string]: string } = {
      'overview': 'Dashboard Enseignant',
      'planning': 'Mon Planning',
      'disponibilites': 'Mes Disponibilités',
      'courses': 'Mes Cours',
      'reservations': 'Réservations de Salles',
      'students': 'Mes Étudiants',
      'notifications': 'Notifications'
    };
    return titles[this.activeSection] || 'Dashboard Enseignant';
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
    
    // Extraire le jour de la semaine
    const jourIndex = dateDebut.getDay();
    const jours = ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
    
    // Extraire la date au format YYYY-MM-DD
    const dateStr = dateDebut.toISOString().split('T')[0];
    
    return {
      date: dateStr,
      jour: jours[jourIndex],
      heureDebut: dateDebut.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' }),
      heureFin: dateFin.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
    };
  }
}
