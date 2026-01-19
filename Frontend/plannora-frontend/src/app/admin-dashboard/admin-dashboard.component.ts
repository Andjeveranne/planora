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
  selectedEnseignant: Enseignant | null = null;
  disponibilitesEnseignant: Disponibilite[] = [];
  showAddEnseignantForm: boolean = false;
  showDisponibilitesModal: boolean = false;
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

  logout(): void {
    this.authService.logout();
  }
}
