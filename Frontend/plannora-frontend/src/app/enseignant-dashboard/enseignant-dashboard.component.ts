import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { PlanningService, EmploiDuTempsDTO } from '../services/planning.service';

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
  jour: string;
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
  imports: [CommonModule, FormsModule],
  templateUrl: './enseignant-dashboard.component.html',
  styleUrls: ['./enseignant-dashboard.component.css']
})
export class EnseignantDashboardComponent implements OnInit {
  userInfo: UserInfo | null = null;
  plannings: Planning[] = [];
  totalHeures: number = 0;
  activeSection: string = 'overview';
  
  // Disponibilités
  disponibilites: Disponibilite[] = [];
  nouvelleDispo: Disponibilite = {
    jour: 'Lundi',
    heureDebut: '',
    heureFin: ''
  };
  joursSemaine: string[] = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];

  // EDT
  weekDays: Day[] = [];
  timeSlots: TimeSlot[] = [];
  currentWeekStart: Date = new Date();
  edtMap: Map<string, EdtSlot> = new Map();
  hasEdt: boolean = false;
  isLoadingEdt: boolean = false;

  constructor(
    private authService: AuthService,
    private planningService: PlanningService
  ) {
    this.initTimeSlots();
  }

  ngOnInit(): void {
    this.userInfo = this.authService.getCurrentUser();
    this.setWeekToCurrent();
    this.loadEdt();

    // Données de démonstration
    this.plannings = [
      { id: 1, cours: 'Mathématiques', salle: 'A101', jour: 'Lundi', heureDebut: '08:00', heureFin: '10:00' },
      { id: 2, cours: 'Physique', salle: 'B205', jour: 'Mardi', heureDebut: '10:00', heureFin: '12:00' },
      { id: 3, cours: 'Mathématiques', salle: 'A101', jour: 'Mercredi', heureDebut: '14:00', heureFin: '16:00' },
      { id: 4, cours: 'Physique', salle: 'B205', jour: 'Jeudi', heureDebut: '08:00', heureFin: '10:00' }
    ];

    this.calculerTotalHeures();
  }

  initTimeSlots(): void {
    this.timeSlots = [];
    for (let h = 8; h < 18; h++) {
      this.timeSlots.push({
        time: `${h}h - ${h + 1}h`,
        startHour: h
      });
    }
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
    
    // Appeler l'API pour récupérer l'EDT de la semaine
    this.planningService.getEdtByEnseignantAndWeek(userId, dateDebut, dateFin).subscribe({
      next: (edtData: EmploiDuTempsDTO[]) => {
        edtData.forEach(edt => {
          const dateKey = `${edt.dateJour}_${edt.heureDebut}h`;
          const slot: EdtSlot = {
            jour: edt.jour,
            heureDebut: `${edt.heureDebut}h`,
            heureFin: `${edt.heureFin}h`,
            ue: edt.ue,
            classe: edt.classe,
            salle: edt.salle,
            type: edt.typeCours,
            dateKey: dateKey
          };
          this.edtMap.set(dateKey, slot);
        });
        this.hasEdt = edtData.length > 0;
        this.isLoadingEdt = false;
      },
      error: (err) => {
        console.error('Erreur chargement EDT:', err);
        this.isLoadingEdt = false;
        this.hasEdt = false;
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
    return this.getTotalCoursWeek(); // Chaque créneau = 1h
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
    const [h, m] = heure.split(':').map(Number);
    return h * 60 + m;
  }

  logout(): void {
    this.authService.logout();
  }

  // Gestion des disponibilités
  ajouterDisponibilite(): void {
    if (this.nouvelleDispo.jour && this.nouvelleDispo.heureDebut && this.nouvelleDispo.heureFin) {
      if (this.nouvelleDispo.heureDebut >= this.nouvelleDispo.heureFin) {
        alert('L\'heure de début doit être avant l\'heure de fin');
        return;
      }
      
      this.disponibilites.push({ ...this.nouvelleDispo });
      // Reset partiel pour faciliter la saisie suivante
      this.nouvelleDispo.heureDebut = '';
      this.nouvelleDispo.heureFin = '';
    }
  }

  supprimerDisponibilite(index: number): void {
    this.disponibilites.splice(index, 1);
  }

  envoyerDisponibilites(): void {
    console.log('Envoi des disponibilités à l\'administrateur:', this.disponibilites);
    alert('Vos disponibilités ont été envoyées à l\'administrateur avec succès !');
    this.disponibilites = []; // Reset après envoi
  }
}
