import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { UserService, Enseignant, EnseignantRequest } from '../services/user.service';
import { PlanningService, DisponibiliteDTO, EmploiDuTempsDTO } from '../services/planning.service';

interface UserInfo {
  nom: string;
  prenom: string;
  email: string;
  role: string;
}

interface StatCard {
  title: string;
  value: number;
  icon: string;
  color: string;
}

interface TimeSlot {
  time: string;
  startHour: number;
}

interface Day {
  name: string;
  date: Date;
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
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  userInfo: UserInfo | null = null;
  activeSection: string = 'overview';
  recentUsers: any[] = [];
  recentRooms: any[] = [];
  enseignants: Enseignant[] = [];
  showAddEnseignantForm: boolean = false;
  isLoadingEnseignants: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  // Modal de disponibilités
  showDisponibilitesModal: boolean = false;
  selectedEnseignant: Enseignant | null = null;
  selectedEnseignantDisponibilites: DisponibiliteDTO[] = [];
  isLoadingDisponibilites: boolean = false;

  // Modal EDT
  showEdtModal: boolean = false;
  edtMode: 'view' | 'generate' = 'view';
  isGeneratingEdt: boolean = false;
  edtMap: Map<string, EdtSlot> = new Map();

  // Grille EDT
  weekDays: Day[] = [];
  timeSlots: TimeSlot[] = [];
  currentWeekStart: Date = new Date();
  disponibilitesMap: Map<string, DisponibiliteDTO> = new Map();

  // Recherche
  searchTerm: string = '';

  newEnseignant: EnseignantRequest = {
    email: '',
    mdp: '',
    nomUser: '',
    prenomUser: '',
    telephone: '',
    role: 'ENSEIGNANT',
    specialite: '',
    departement: ''
  };

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private planningService: PlanningService
  ) {
    this.initTimeSlots();
  }

  ngOnInit(): void {
    this.userInfo = this.authService.getCurrentUser();
    this.setWeekToCurrent();

    // Données de démonstration
    this.recentUsers = [
      { name: 'Jesse Thomas', points: '637 Points', accuracy: '98% Correct', initials: 'JT', color: '#667eea' },
      { name: 'Thisal Mathiyazhagan', points: '637 Points', accuracy: '89% Correct', initials: 'TM', color: '#f093fb' },
      { name: 'Helen Chuang', points: '637 Points', accuracy: '88% Correct', initials: 'HC', color: '#4facfe' },
      { name: 'Lura Silverman', points: '637 Points', accuracy: '', initials: 'LS', color: '#43e97b' },
      { name: 'Winifred Groton', points: '637 Points', accuracy: '', initials: 'WG', color: '#f59e0b' }
    ];

    this.recentRooms = [
      { name: 'Salle 305 Batiment Secondaire R3', capacity: '50', type: 'Laboratoire' },
      { name: 'Salle 305 Batiment Secondaire R3', capacity: '50', type: 'Travaux Pratiques' },
      { name: 'Salle 305 Batiment Secondaire R3', capacity: '50', type: 'Transversale' },
      { name: 'Salle 305 Batiment Secondaire R3', capacity: '50', type: '' },
      { name: 'Salle 305 Batiment Secondaire R3', capacity: '50', type: '' }
    ];
  }

  initTimeSlots() {
    this.timeSlots = [];
    for (let h = 8; h < 17; h++) {
      this.timeSlots.push({
        time: `${h}h - ${h + 1}h`,
        startHour: h
      });
    }
  }

  setWeekToCurrent() {
    const now = new Date();
    const day = now.getDay();
    const diff = now.getDate() - day + (day === 0 ? -6 : 1);
    this.currentWeekStart = new Date(now.setDate(diff));
    this.currentWeekStart.setHours(0, 0, 0, 0);
    this.updateWeekDays();
  }

  changeWeek(offset: number) {
    const newDate = new Date(this.currentWeekStart);
    newDate.setDate(newDate.getDate() + (offset * 7));
    this.currentWeekStart = newDate;
    this.updateWeekDays();
    if (this.selectedEnseignant) {
      if (this.showDisponibilitesModal) {
        this.loadEnseignantDisponibilites(this.selectedEnseignant);
      }
      if (this.showEdtModal && this.edtMode === 'view') {
        this.loadEdt(this.selectedEnseignant);
      }
    }
  }

  updateWeekDays() {
    this.weekDays = [];
    const days = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
    for (let i = 0; i < 6; i++) {
      const date = new Date(this.currentWeekStart);
      date.setDate(date.getDate() + i);
      this.weekDays.push({ name: days[i], date: date });
    }
  }

  setActiveSection(section: string): void {
    this.activeSection = section;
    if (section === 'enseignants') {
      this.loadEnseignants();
    }
  }

  loadEnseignants(): void {
    this.isLoadingEnseignants = true;
    this.errorMessage = '';

    this.userService.getEnseignants().subscribe({
      next: (data) => {
        this.enseignants = data;
        this.isLoadingEnseignants = false;
      },
      error: (error) => {
        console.error('❌ Erreur lors du chargement des enseignants:', error);
        let errorMsg = 'Erreur lors du chargement des enseignants';
        if (error.status === 401) errorMsg = 'Non autorisé - Veuillez vous reconnecter';
        else if (error.status === 403) errorMsg = 'Accès refusé - Permissions insuffisantes';
        else if (error.status === 404) errorMsg = 'Service non trouvé';
        else if (error.status === 0) errorMsg = 'Impossible de contacter le serveur';
        this.errorMessage = errorMsg;
        this.isLoadingEnseignants = false;
      }
    });
  }

  // Filtre les enseignants selon la recherche
  get filteredEnseignants(): Enseignant[] {
    if (!this.searchTerm) return this.enseignants;
    const term = this.searchTerm.toLowerCase();
    return this.enseignants.filter(e =>
      e.nomUser?.toLowerCase().includes(term) ||
      e.prenomUser?.toLowerCase().includes(term) ||
      e.email?.toLowerCase().includes(term) ||
      e.specialite?.toLowerCase().includes(term)
    );
  }

  toggleAddEnseignantForm(): void {
    this.showAddEnseignantForm = !this.showAddEnseignantForm;
    if (!this.showAddEnseignantForm) {
      this.resetForm();
    }
  }

  resetForm(): void {
    this.newEnseignant = {
      email: '', mdp: '', nomUser: '', prenomUser: '',
      telephone: '', role: 'ENSEIGNANT', specialite: '', departement: ''
    };
    this.errorMessage = '';
    this.successMessage = '';
  }

  ajouterEnseignant(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.validateForm()) return;

    this.userService.creerEnseignant(this.newEnseignant).subscribe({
      next: (enseignant) => {
        this.successMessage = 'Enseignant ajouté avec succès!';
        this.enseignants.push(enseignant);
        this.resetForm();
        this.showAddEnseignantForm = false;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        let errorMsg = 'Erreur lors de l\'ajout de l\'enseignant';
        if (error.status === 401) errorMsg = 'Non autorisé';
        else if (error.status === 403) errorMsg = 'Accès refusé';
        else if (error.error?.message) errorMsg = error.error.message;
        this.errorMessage = errorMsg;
      }
    });
  }

  validateForm(): boolean {
    if (!this.newEnseignant.email || !this.newEnseignant.mdp ||
      !this.newEnseignant.nomUser || !this.newEnseignant.prenomUser ||
      !this.newEnseignant.telephone) {
      this.errorMessage = 'Tous les champs obligatoires doivent être remplis';
      return false;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.newEnseignant.email)) {
      this.errorMessage = 'Email invalide';
      return false;
    }

    if (this.newEnseignant.mdp.length < 6) {
      this.errorMessage = 'Le mot de passe doit contenir au moins 6 caractères';
      return false;
    }

    return true;
  }

  deleteEnseignant(id: string): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cet enseignant? Cette action est irréversible.')) {
      return;
    }

    this.userService.deleteEnseignant(id).subscribe({
      next: () => {
        this.enseignants = this.enseignants.filter(e => e.idUser !== id);
        this.successMessage = 'Enseignant supprimé avec succès!';
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (error) => {
        console.error('Erreur lors de la suppression:', error);
        this.errorMessage = 'Erreur lors de la suppression de l\'enseignant';
      }
    });
  }

  // ========== GESTION DES DISPONIBILITÉS ==========

  viewDisponibilites(enseignant: Enseignant): void {
    this.selectedEnseignant = enseignant;
    this.showDisponibilitesModal = true;
    this.setWeekToCurrent();
    this.loadEnseignantDisponibilites(enseignant);
  }

  closeDisponibilitesModal(): void {
    this.showDisponibilitesModal = false;
    this.selectedEnseignant = null;
    this.disponibilitesMap.clear();
  }

  loadEnseignantDisponibilites(enseignant: Enseignant): void {
    if (!enseignant.idUser) return;

    this.isLoadingDisponibilites = true;
    this.planningService.getDisponibilites(enseignant.idUser).subscribe({
      next: (data) => {
        this.disponibilitesMap.clear();
        data.forEach(d => {
          const start = new Date(d.dateHeureDebut);
          const key = this.getKey(start);
          this.disponibilitesMap.set(key, d);
        });
        this.isLoadingDisponibilites = false;
      },
      error: (err) => {
        console.error('Erreur chargement disponibilités:', err);
        this.isLoadingDisponibilites = false;
      }
    });
  }

  getKey(date: Date): string {
    const y = date.getFullYear();
    const m = (date.getMonth() + 1).toString().padStart(2, '0');
    const d = date.getDate().toString().padStart(2, '0');
    const h = date.getHours().toString().padStart(2, '0');
    return `${y}-${m}-${d}_${h}`;
  }

  getKeyForSlot(day: Day, slot: TimeSlot): string {
    const date = new Date(day.date);
    date.setHours(slot.startHour, 0, 0, 0);
    return this.getKey(date);
  }

  isAvailable(day: Day, slot: TimeSlot): boolean {
    return this.disponibilitesMap.has(this.getKeyForSlot(day, slot));
  }

  // Compte le total de créneaux disponibles pour la semaine affichée
  getTotalDisponibilites(): number {
    let count = 0;
    for (const day of this.weekDays) {
      for (const slot of this.timeSlots) {
        if (this.isAvailable(day, slot)) {
          count++;
        }
      }
    }
    return count;
  }

  // Génère l'emploi du temps (pour l'instant: affiche un message / export)
  generateSchedule(): void {
    if (!this.selectedEnseignant) return;

    const dispos = Array.from(this.disponibilitesMap.values());

    if (dispos.length === 0) {
      alert('Cet enseignant n\'a renseigné aucune disponibilité. Impossible de générer un EDT.');
      return;
    }

    // Créer un résumé de l'EDT
    let summary = `📅 EMPLOI DU TEMPS - ${this.selectedEnseignant.prenomUser} ${this.selectedEnseignant.nomUser}\n`;
    summary += `Spécialité: ${this.selectedEnseignant.specialite || 'Non renseignée'}\n`;
    summary += `Département: ${this.selectedEnseignant.departement || 'Non renseigné'}\n\n`;
    summary += `Total créneaux disponibles: ${dispos.length}\n\n`;
    summary += `=== CRÉNEAUX DISPONIBLES ===\n`;

    // Grouper par jour
    const grouped = new Map<string, string[]>();
    dispos.forEach(d => {
      const start = new Date(d.dateHeureDebut);
      const dateStr = start.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' });
      const timeStr = `${start.getHours()}h - ${start.getHours() + 1}h`;

      if (!grouped.has(dateStr)) {
        grouped.set(dateStr, []);
      }
      grouped.get(dateStr)!.push(timeStr);
    });

    grouped.forEach((times, date) => {
      summary += `\n${date}:\n`;
      times.sort().forEach(t => summary += `  • ${t}\n`);
    });

    alert(summary);

    // TODO: Intégrer avec un système de génération d'EDT plus avancé
    this.successMessage = `EDT généré pour ${this.selectedEnseignant.prenomUser} ${this.selectedEnseignant.nomUser}`;
    setTimeout(() => this.successMessage = '', 4000);
  }

  // Export CSV des disponibilités
  exportDisponibilites(): void {
    if (!this.selectedEnseignant) return;

    const dispos = Array.from(this.disponibilitesMap.values());
    if (dispos.length === 0) {
      alert('Aucune disponibilité à exporter.');
      return;
    }

    let csv = 'Date,Heure Début,Heure Fin,Enseignant\n';
    dispos.forEach(d => {
      const start = new Date(d.dateHeureDebut);
      const end = new Date(d.dateHeureFin);
      csv += `${start.toLocaleDateString('fr-FR')},${start.toLocaleTimeString('fr-FR')},${end.toLocaleTimeString('fr-FR')},${this.selectedEnseignant?.prenomUser} ${this.selectedEnseignant?.nomUser}\n`;
    });

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `disponibilites_${this.selectedEnseignant.nomUser}_${new Date().toISOString().split('T')[0]}.csv`;
    link.click();
  }

  // ========== GESTION DE L'EDT ==========

  openEdtModal(enseignant: Enseignant, mode: 'view' | 'generate'): void {
    this.selectedEnseignant = enseignant;
    this.edtMode = mode;
    this.showEdtModal = true;
    this.setWeekToCurrent();
    
    if (mode === 'view') {
      this.loadEdt(enseignant);
    } else {
      this.loadEnseignantDisponibilites(enseignant);
    }
  }

  closeEdtModal(): void {
    this.showEdtModal = false;
    this.selectedEnseignant = null;
    this.edtMap.clear();
  }

  loadEdt(enseignant: Enseignant): void {
    if (!enseignant.idUser) return;
    
    this.edtMap.clear();
    
    // Calculer les dates de début et fin de semaine
    const dateDebut = this.formatDate(this.currentWeekStart);
    const weekEnd = new Date(this.currentWeekStart);
    weekEnd.setDate(weekEnd.getDate() + 6);
    const dateFin = this.formatDate(weekEnd);
    
    // Charger l'EDT depuis l'API pour la semaine en cours
    this.planningService.getEdtByEnseignantAndWeek(enseignant.idUser, dateDebut, dateFin).subscribe({
      next: (edtData) => {
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
      },
      error: (err) => {
        console.error('Erreur chargement EDT:', err);
      }
    });
  }

  getEdtSlot(day: Day, slot: TimeSlot): EdtSlot | null {
    const dateKey = this.formatDate(day.date) + `_${slot.startHour}h`;
    return this.edtMap.get(dateKey) || null;
  }

  formatDate(date: Date): string {
    const y = date.getFullYear();
    const m = (date.getMonth() + 1).toString().padStart(2, '0');
    const d = date.getDate().toString().padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  getDateKey(date: Date, hour: number): string {
    return this.formatDate(date) + `_${hour}h`;
  }

  hasEdtSlot(day: Day, slot: TimeSlot): boolean {
    return this.getEdtSlot(day, slot) !== null;
  }

  // Compte le total de cours pour la semaine affichée
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

  generateEdtFromDisponibilites(): void {
    if (!this.selectedEnseignant || !this.selectedEnseignant.idUser) return;

    const totalDispos = this.getTotalDisponibilites();
    if (totalDispos === 0) {
      alert('Cet enseignant n\'a renseigné aucune disponibilité. Impossible de générer un EDT.');
      return;
    }

    this.isGeneratingEdt = true;

    // Appeler l'API pour générer l'EDT
    this.planningService.genererEdt(this.selectedEnseignant.idUser).subscribe({
      next: (edtData) => {
        this.edtMap.clear();
        
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

        this.isGeneratingEdt = false;
        this.edtMode = 'view';
        this.successMessage = `EDT généré avec succès pour ${this.selectedEnseignant!.prenomUser} ${this.selectedEnseignant!.nomUser} (${edtData.length} cours)`;
        setTimeout(() => this.successMessage = '', 4000);
      },
      error: (err) => {
        console.error('Erreur génération EDT:', err);
        this.isGeneratingEdt = false;
        this.errorMessage = 'Erreur lors de la génération de l\'EDT';
        setTimeout(() => this.errorMessage = '', 4000);
      }
    });
  }

  getDayName(dayIndex: number): string {
    const days = ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
    return days[dayIndex];
  }

  hasExistingEdt(enseignant: Enseignant): boolean {
    // Pour l'instant on retourne true si l'enseignant existe
    // Une amélioration serait de faire un appel API pour vérifier
    return !!enseignant.idUser;
  }

  logout(): void {
    this.authService.logout();
  }
}
