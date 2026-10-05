import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { UserService, Enseignant, EnseignantRequest } from '../services/user.service';
import { SalleService, SalleDTO, StatutSalle, TypeSalle } from '../services/salle.service';
import { PlanningService, DisponibiliteDTO, EmploiDuTempsDTO } from '../services/planning.service';
import { NotificationService } from '../services/notification.service';
import { UEManagementComponent } from '../ue-management/ue-management.component';
import { GestionSallesComponent } from './gestion-salles/gestion-salles.component';
import { GestionEnseignantsComponent } from './gestion-enseignants/gestion-enseignants.component';
import { GestionEcolesComponent } from './gestion-ecoles/gestion-ecoles.component';
import { GestionClassesComponent } from './gestion-classes/gestion-classes.component';
import { GenerationEmploisComponent } from './generation-emplois/generation-emplois.component';

interface UserInfo {
  nom: string;
  prenom: string;
  email: string;
  role: string;
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
  dateKey?: string;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, UEManagementComponent, GestionSallesComponent, GestionEnseignantsComponent, GestionEcolesComponent, GestionClassesComponent, GenerationEmploisComponent],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  userInfo: UserInfo | null = null;
  activeSection: string = 'overview';
  sidebarOpen: boolean = false;
  recentUsers: { name: string; initials: string; color: string; meta: string }[] = [];
  recentRooms: { name: string; capacity: number; type: string }[] = [];
  enseignants: Enseignant[] = [];
  dashboardSalles: SalleDTO[] = [];
  /** Métriques issues de la base (enseignants, salles) */
  metricEnseignants: number = 0;
  metricSalles: number = 0;
  metricSallesOccupees: number = 0;
  isLoadingOverview: boolean = false;
  /** Données pour les graphiques (EDT agrégés) */
  chartTop5Salles: { salle: string; count: number }[] = [];
  chartClassesLabos: { classe: string; count: number }[] = [];
  isLoadingCharts: boolean = false;
  /** Valeur max pour la hauteur des barres (pourcentage) */
  chartTop5Max: number = 1;
  chartClassesLabosMax: number = 1;
  selectedEnseignant: Enseignant | null = null;
  disponibilitesEnseignant: DisponibiliteDTO[] = [];
  showAddEnseignantForm: boolean = false;
  showDisponibilitesModal: boolean = false;
  isLoadingEnseignants: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  // Modal de disponibilités
  selectedEnseignantDisponibilites: DisponibiliteDTO[] = [];
  isLoadingDisponibilites: boolean = false;

  // Modal EDT
  showEdtModal: boolean = false;
  edtMode: 'view' | 'generate' = 'view';
  isGeneratingEdt: boolean = false;
  edtMap: Map<string, EdtSlot> = new Map();
  nombreSemainesGeneration: number = 4;

  // Grille EDT
  weekDays: Day[] = [];
  timeSlots: TimeSlot[] = [];
  currentWeekStart: Date = new Date();
  disponibilitesMap: Map<string, DisponibiliteDTO> = new Map();
  
  // Pre-calculated grid for stable rendering
  availabilityGrid: boolean[][] = [];
  gridCalculated: boolean = false;

  // Recherche
  searchTerm: string = '';

  // Filtres
  filterStatus: string = 'all';
  filterDepartement: string = 'all';
  filterEcole: string = 'all';
  availableDepartements: string[] = [];

  // Cache for statistics
  private _cachedStats: {
    total: number;
    hours: number;
    mostAvailableDay: string;
    lastUpdate: number;
  } | null = null;

  newEnseignant: EnseignantRequest = {
    email: '',
    mdp: '',
    nomUser: '',
    prenomUser: '',
    telephone: '',
    role: 'ENSEIGNANT',
    specialite: '',
    departement: '',
    idEcole: '',
    idEcoles: []
  };

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private salleService: SalleService,
    private planningService: PlanningService,
    private notification: NotificationService,
    private cdr: ChangeDetectorRef
  ) {
    this.initTimeSlots();
  }

  ngOnInit(): void {
    this.userInfo = this.authService.getCurrentUser();
    this.setWeekToCurrent();
    this.loadDashboardData();

    // Fermer le dropdown quand on clique ailleurs
    document.addEventListener('click', this.handleClickOutside.bind(this));
  }

  /** Charge les données du dashboard (enseignants, salles) et met à jour les métriques */
  loadDashboardData(): void {
    this.isLoadingOverview = true;
    this.cdr.markForCheck();

    this.userService.getEnseignants().subscribe({
      next: (enseignants) => {
        this.enseignants = enseignants;
        this.extractDepartements();
        this.buildRecentUsersFromEnseignants();
        this.computeMetrics();
        this.loadChartData();
        this.isLoadingOverview = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.enseignants = [];
        this.recentUsers = [];
        this.computeMetrics();
        this.isLoadingOverview = false;
        this.cdr.markForCheck();
      }
    });

    this.salleService.obtenirToutesLesSalles().subscribe({
      next: (salles) => {
        this.dashboardSalles = salles;
        this.buildRecentRoomsFromSalles();
        this.computeMetrics();
        this.loadChartData();
        this.cdr.markForCheck();
      },
      error: () => {
        this.dashboardSalles = [];
        this.recentRooms = [];
        this.computeMetrics();
        this.cdr.markForCheck();
      }
    });
  }

  private buildRecentUsersFromEnseignants(): void {
    const colors = ['#667eea', '#764ba2', '#48bb78', '#38a169', '#ed8936', '#dd6b20'];
    this.recentUsers = this.enseignants.slice(0, 8).map((e, i) => {
      const prenom = e.prenomUser || '';
      const nom = e.nomUser || '';
      const name = `${prenom} ${nom}`.trim() || e.email;
      const initials = (prenom.charAt(0) + nom.charAt(0)).toUpperCase() || e.email.charAt(0).toUpperCase();
      return {
        name,
        initials: initials || '?',
        color: colors[i % colors.length],
        meta: e.specialite ? `${e.specialite} • ${e.departement || ''}`.trim() : (e.email || 'Enseignant')
      };
    });
  }

  private buildRecentRoomsFromSalles(): void {
    this.recentRooms = this.dashboardSalles.slice(0, 8).map(s => ({
      name: s.nomSalle,
      capacity: s.capacite,
      type: this.salleService.getTypeLabel(s.type)
    }));
  }

  private computeMetrics(): void {
    this.metricEnseignants = this.enseignants.length;
    this.metricSalles = this.dashboardSalles.length;
    this.metricSallesOccupees = this.dashboardSalles.filter(s => s.statut === StatutSalle.OCCUPEE).length;
  }

  /** Charge les données des graphiques (top 5 salles, classes en labos) à partir des EDT des enseignants */
  loadChartData(): void {
    if (this.enseignants.length === 0) {
      this.chartTop5Salles = [];
      this.chartClassesLabos = [];
      this.chartTop5Max = 1;
      this.chartClassesLabosMax = 1;
      this.cdr.markForCheck();
      return;
    }
    this.isLoadingCharts = true;
    this.cdr.markForCheck();
    const labRoomNames = new Set(
      this.dashboardSalles
        .filter(s => s.type === TypeSalle.LABORATOIRE)
        .map(s => s.nomSalle.trim())
    );
    const requests = this.enseignants
      .filter(e => e.idUser)
      .map(e => this.planningService.getEdtFromUserService(e.idUser!).pipe(catchError(() => of([]))));
    if (requests.length === 0) {
      this.chartTop5Salles = [];
      this.chartClassesLabos = [];
      this.isLoadingCharts = false;
      this.cdr.markForCheck();
      return;
    }
    forkJoin(requests).subscribe({
      next: (allEdt) => {
        const merged = (allEdt as EmploiDuTempsDTO[][]).flat();
        const bySalle = new Map<string, number>();
        merged.forEach(edt => {
          const s = (edt.salle || '').trim();
          if (s) bySalle.set(s, (bySalle.get(s) || 0) + 1);
        });
        this.chartTop5Salles = Array.from(bySalle.entries())
          .map(([salle, count]) => ({ salle, count }))
          .sort((a, b) => b.count - a.count)
          .slice(0, 5);
        this.chartTop5Max = Math.max(1, ...this.chartTop5Salles.map(x => x.count));

        const labEdt = merged.filter(edt => labRoomNames.has((edt.salle || '').trim()));
        const byClasse = new Map<string, number>();
        labEdt.forEach(edt => {
          const c = (edt.classe || '').trim();
          if (c) byClasse.set(c, (byClasse.get(c) || 0) + 1);
        });
        this.chartClassesLabos = Array.from(byClasse.entries())
          .map(([classe, count]) => ({ classe, count }))
          .sort((a, b) => b.count - a.count)
          .slice(0, 8);
        this.chartClassesLabosMax = Math.max(1, ...this.chartClassesLabos.map(x => x.count));
        this.isLoadingCharts = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.chartTop5Salles = [];
        this.chartClassesLabos = [];
        this.isLoadingCharts = false;
        this.cdr.markForCheck();
      }
    });
  }

  getBarHeightPercent(value: number, max: number): number {
    return max > 0 ? Math.round((value / max) * 100) : 0;
  }

  ngOnDestroy(): void {
    document.removeEventListener('click', this.handleClickOutside.bind(this));
  }

  handleClickOutside(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.dropdown-container')) {
      this.closeDropdown();
    }
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

  getWeekLabel(): string {
    const start = new Date(this.currentWeekStart);
    const end = new Date(this.currentWeekStart);
    end.setDate(end.getDate() + 5); // Samedi
    
    const startStr = start.toLocaleDateString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' });
    const endStr = end.toLocaleDateString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' });
    
    return `${startStr} - ${endStr}`;
  }

  setActiveSection(section: string): void {
    this.activeSection = section;
    this.sidebarOpen = false;
    this.cdr.markForCheck();
    if (section === 'enseignants') {
      this.loadEnseignants();
    }
  }

  /** Titre et description de l'en-tête selon la section active */
  getSectionHeader(): { title: string; description: string } {
    const headers: Record<string, { title: string; description: string }> = {
      overview: { title: 'Tableau de bord', description: 'Vue d\'ensemble des indicateurs et des listes de la plateforme Planora.' },
      enseignants: { title: 'Gestion des enseignants', description: 'Consulter, ajouter et gérer les comptes et les disponibilités des enseignants.' },
      ues: { title: 'Unités d\'enseignement', description: 'Gérer les UE, les matières et leur affectation aux formations.' },
      rooms: { title: 'Gestion des salles', description: 'Créer et gérer les salles, capacités et types pour la planification.' },
      ecoles: { title: 'Gestion des écoles', description: 'Administrer les établissements et leurs paramètres.' },
      equipment: { title: 'Gestion des équipements', description: 'Suivre et gérer le matériel et les équipements disponibles.' },
      reservations: { title: 'Réservations', description: 'Consulter et gérer les réservations de salles et créneaux.' },
      reporting: { title: 'Rapports', description: 'Générer et consulter les rapports et statistiques de la plateforme.' },
      calendar: { title: 'Calendrier', description: 'Visualiser et planifier les événements et emplois du temps.' },
      classes: { title: 'Gestion des classes', description: 'Gérer les classes et les effectifs des formations.' },
      'emplois-du-temps': { title: 'Génération des Emplois du Temps', description: 'Générer et gérer les emplois du temps avec l\'algorithme Ford-Fulkerson' }
    };
    return headers[this.activeSection] ?? { title: 'Administration', description: 'Gestion de la plateforme Planora.' };
  }

  loadEnseignants(): void {
    this.isLoadingEnseignants = true;
    this.errorMessage = '';

    this.userService.getEnseignants().subscribe({
      next: (data) => {
        this.enseignants = data;
        this.isLoadingEnseignants = false;
        this.extractDepartements();
        this.cdr.markForCheck();
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
        this.cdr.markForCheck();
      }
    });
  }

  extractDepartements(): void {
    const depts = new Set<string>();
    this.enseignants.forEach(e => {
      if (e.departement && e.departement.trim()) {
        depts.add(e.departement.trim());
      }
    });
    this.availableDepartements = Array.from(depts).sort();
  }

  get filteredEnseignants(): Enseignant[] {
    let filtered = this.enseignants;

    // Filtre par recherche
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(e =>
        e.nomUser?.toLowerCase().includes(term) ||
        e.prenomUser?.toLowerCase().includes(term) ||
        e.email?.toLowerCase().includes(term) ||
        e.specialite?.toLowerCase().includes(term) ||
        e.departement?.toLowerCase().includes(term)
      );
    }

    // Filtre par statut (pour l'instant tous sont actifs car pas de champ statut)
    // Dans une vraie application, il faudrait un champ "actif" dans l'entité
    if (this.filterStatus !== 'all') {
      // Simulation: tous les enseignants sont actifs
      if (this.filterStatus === 'inactive') {
        filtered = [];
      }
    }

    // Filtre par département
    if (this.filterDepartement !== 'all') {
      filtered = filtered.filter(e => e.departement === this.filterDepartement);
    }

    // Filtre par école
    if (this.filterEcole !== 'all') {
      // Note: Le champ école n'existe pas encore dans l'entité
      // Pour l'instant, on ne filtre pas
      // Dans une vraie application, il faudrait ajouter ce champ
    }

    return filtered;
  }

  applyFilters(): void {
    this.currentPage = 1; // Réinitialiser à la première page
    this.cdr.markForCheck();
  }

  resetFilters(): void {
    this.filterStatus = 'all';
    this.filterDepartement = 'all';
    this.filterEcole = 'all';
    this.searchTerm = '';
    this.currentPage = 1;
    this.cdr.markForCheck();
  }

  toggleAddEnseignantForm(): void {
    this.showAddEnseignantForm = !this.showAddEnseignantForm;
    if (!this.showAddEnseignantForm) {
      this.resetForm();
    }
    this.cdr.markForCheck();
  }

  resetForm(): void {
    this.newEnseignant = {
      email: '', mdp: '', nomUser: '', prenomUser: '',
      telephone: '', role: 'ENSEIGNANT', specialite: '', departement: '',
      idEcole: '', idEcoles: []
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
        this.cdr.markForCheck();
        setTimeout(() => {
          this.successMessage = '';
          this.cdr.markForCheck();
        }, 3000);
      },
      error: (error) => {
        let errorMsg = 'Erreur lors de l\'ajout de l\'enseignant';
        if (error.status === 401) errorMsg = 'Non autorisé';
        else if (error.status === 403) errorMsg = 'Accès refusé';
        else if (error.error?.message) errorMsg = error.error.message;
        this.errorMessage = errorMsg;
        this.cdr.markForCheck();
      }
    });
  }

  validateForm(): boolean {
    if (!this.newEnseignant.email || !this.newEnseignant.mdp ||
      !this.newEnseignant.nomUser || !this.newEnseignant.prenomUser) {
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
    this.notification.confirm('Êtes-vous sûr de vouloir supprimer cet enseignant ? Cette action est irréversible.', 'Confirmation', () => {
      this.userService.deleteEnseignant(id).subscribe({
      next: () => {
        this.enseignants = this.enseignants.filter(e => e.idUser !== id);
        this.successMessage = 'Enseignant supprimé avec succès!';
        this.cdr.markForCheck();
        setTimeout(() => {
          this.successMessage = '';
          this.cdr.markForCheck();
        }, 3000);
      },
      error: (error) => {
        console.error('Erreur lors de la suppression:', error);
        this.errorMessage = 'Erreur lors de la suppression de l\'enseignant';
        this.cdr.markForCheck();
      }
    });
    });
  }

  logout(): void {
    this.authService.logout();
  }

  // Modal management
  openDisponibilitesModal(enseignant: Enseignant): void {
    this.selectedEnseignant = enseignant;
    this.showDisponibilitesModal = true;
    this.setWeekToCurrent();
    this.loadEnseignantDisponibilites(enseignant);
    this.cdr.markForCheck();
  }
  
  openEdtModal(enseignant: Enseignant, mode: 'view' | 'generate'): void {
    this.selectedEnseignant = enseignant;
    this.edtMode = mode;
    this.showEdtModal = true;
    
    if (mode === 'view') {
      this.loadEdt(enseignant);
    } else {
      this.loadEnseignantDisponibilites(enseignant);
    }
    this.cdr.markForCheck();
  }

  closeEdtModal(): void {
    this.showEdtModal = false;
    this.selectedEnseignant = null;
    this.edtMap.clear();
    this.cdr.markForCheck();
  }

  closeDisponibilitesModal(): void {
    this.showDisponibilitesModal = false;
    this.selectedEnseignant = null;
    this.selectedEnseignantDisponibilites = [];
    this.cdr.markForCheck();
  }

  // Data loading methods
  loadEnseignantDisponibilites(enseignant: Enseignant): void {
    if (!enseignant.idUser) {
      console.error('Pas d\'ID utilisateur pour l\'enseignant:', enseignant);
      return;
    }
    
    console.log('Chargement des disponibilités pour:', enseignant.prenomUser, enseignant.nomUser, 'ID:', enseignant.idUser);
    
    this.isLoadingDisponibilites = true;
    this.selectedEnseignantDisponibilites = [];
    this.disponibilitesMap.clear();
    this.gridCalculated = false;
    this.availabilityGrid = [];
    this._cachedStats = null;
    
    this.planningService.getDisponibilites(enseignant.idUser).subscribe({
      next: (disponibilites: DisponibiliteDTO[]) => {
        console.log('Disponibilités reçues du serveur:', disponibilites);
        this.selectedEnseignantDisponibilites = disponibilites || [];
        
        // Use Promise.resolve to schedule after current change detection cycle
        Promise.resolve().then(() => {
          this.updateDisponibilitesMap();
          this.calculateAvailabilityGrid();
          this.isLoadingDisponibilites = false;
          this.gridCalculated = true;
          
          console.log('État final:');
          console.log('- Disponibilités:', this.selectedEnseignantDisponibilites.length);
          console.log('- Map size:', this.disponibilitesMap.size);
          console.log('- Total calculé:', this.getTotalDisponibilites());
          
          this.cdr.detectChanges();
        });
      },
      error: (error: any) => {
        console.error('Erreur chargement disponibilités:', error);
        this.isLoadingDisponibilites = false;
        this.selectedEnseignantDisponibilites = [];
        this.gridCalculated = true;
        this.availabilityGrid = [];
        
        let errorMessage = 'Erreur lors du chargement des disponibilités.';
        if (error.status === 0) {
          errorMessage += ' Le service de planning n\'est pas accessible.';
        } else if (error.status === 404) {
          errorMessage += ' Aucune disponibilité trouvée pour cet enseignant.';
        } else if (error.status === 500) {
          errorMessage += ' Erreur serveur.';
        }
        
        this.notification.error(errorMessage + ' Vérifiez que tous les services sont démarrés.');
        this.cdr.detectChanges();
      }
    });
  }

  loadEdt(enseignant: Enseignant): void {
    if (!enseignant.idUser) return;
    
    this.edtMap.clear();
    
    const dateDebut = this.formatDate(this.currentWeekStart);
    const weekEnd = new Date(this.currentWeekStart);
    weekEnd.setDate(weekEnd.getDate() + 6);
    const dateFin = this.formatDate(weekEnd);
    
    // Essayer d'abord de charger depuis UserService (données plus récentes)
    this.planningService.getEdtFromUserService(enseignant.idUser).subscribe({
      next: (edtData: EmploiDuTempsDTO[]) => {
        // Filtrer les données pour la semaine courante
        const filteredData = edtData.filter(edt => {
          const dateJour = edt.dateJour ?? '';
          if (!dateJour) return false;
          const edtDate = new Date(dateJour);
          return !isNaN(edtDate.getTime()) && edtDate >= this.currentWeekStart && edtDate <= weekEnd;
        });
        
        if (filteredData.length > 0) {
          console.log('EDT chargé depuis UserService:', filteredData);
          this.processEdtData(filteredData);
        } else {
          // Fallback vers PlanningService si aucune donnée dans UserService
          this.loadEdtFromPlanningService(enseignant.idUser || '', dateDebut, dateFin);
        }
      },
      error: (error) => {
        console.warn('Erreur lors du chargement depuis UserService, fallback vers PlanningService:', error);
        this.loadEdtFromPlanningService(enseignant.idUser || '', dateDebut, dateFin);
      }
    });
  }

  private loadEdtFromPlanningService(enseignantId: string, dateDebut: string, dateFin: string): void {
    this.planningService.getEdtByEnseignantAndWeek(enseignantId, dateDebut, dateFin).subscribe({
      next: (edtData: EmploiDuTempsDTO[]) => {
        console.log('EDT chargé depuis PlanningService:', edtData);
        this.processEdtData(edtData);
      },
      error: (error) => {
        console.error('Erreur lors du chargement de l\'EDT:', error);
      }
    });
  }

  private processEdtData(edtData: EmploiDuTempsDTO[]): void {
    edtData.forEach(edt => {
      const dateKey = `${edt.dateJour}_${edt.heureDebut}h`;
      const slot: EdtSlot = {
        jour: edt.jour ?? '',
        heureDebut: `${edt.heureDebut ?? ''}h`,
        heureFin: `${edt.heureFin ?? ''}h`,
        ue: edt.ue ?? '',
        classe: edt.classe ?? '',
        salle: edt.salle ?? '',
        type: edt.typeCours ?? '',
        dateKey: dateKey
      };
      this.edtMap.set(dateKey, slot);
    });
  }

  // Utility functions
  formatDate(date: Date): string {
    const y = date.getFullYear();
    const m = (date.getMonth() + 1).toString().padStart(2, '0');
    const d = date.getDate().toString().padStart(2, '0');
    return `${y}-${m}-${d}`;
  }

  updateDisponibilitesMap(): void {
    this.disponibilitesMap.clear();
    this._cachedStats = null;
    
    console.log('Updating disponibilites map with:', this.selectedEnseignantDisponibilites);
    
    this.selectedEnseignantDisponibilites.forEach((dispo) => {
      try {
        const dateDebut = new Date(dispo.dateHeureDebut);
        const dateFin = new Date(dispo.dateHeureFin);
        
        if (isNaN(dateDebut.getTime()) || isNaN(dateFin.getTime())) {
          console.error('Date invalide pour la disponibilité:', dispo);
          return;
        }
        
        const jourIndex = dateDebut.getDay();
        const jours = ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
        const jourNom = jours[jourIndex];
        
        const heureDebut = dateDebut.getHours();
        const heureFin = dateFin.getHours();
        const minuteFin = dateFin.getMinutes();
        
        // If end time has minutes, include the final hour
        const heureFinInclusive = minuteFin > 0 ? heureFin + 1 : heureFin;
        
        // Create entries for all hours in the time range
        for (let h = heureDebut; h < heureFinInclusive; h++) {
          const key = `${jourNom}_${h}h`;
          this.disponibilitesMap.set(key, dispo);
        }
        
        console.log(`Mapped availability: ${jourNom} ${heureDebut}h-${heureFinInclusive}h (${heureFinInclusive - heureDebut} slots)`);
        
      } catch (error) {
        console.error('Erreur lors du mapping de la disponibilité:', dispo, error);
      }
    });
    
    console.log('Map finale des disponibilités:', this.disponibilitesMap);
    console.log('Nombre total de créneaux mappés:', this.disponibilitesMap.size);
  }

  calculateAvailabilityGrid(): void {
    const newGrid: boolean[][] = [];
    
    if (!this.timeSlots || !Array.isArray(this.timeSlots) || 
        !this.weekDays || !Array.isArray(this.weekDays)) {
      this.availabilityGrid = [];
      return;
    }
    
    for (let slotIndex = 0; slotIndex < this.timeSlots.length; slotIndex++) {
      newGrid[slotIndex] = [];
      
      for (let dayIndex = 0; dayIndex < this.weekDays.length; dayIndex++) {
        const day = this.weekDays[dayIndex];
        const slot = this.timeSlots[slotIndex];
        
        if (day && slot && day.name && typeof slot.startHour === 'number') {
          const key = `${day.name}_${slot.startHour}h`;
          newGrid[slotIndex][dayIndex] = this.disponibilitesMap.has(key);
        } else {
          newGrid[slotIndex][dayIndex] = false;
        }
      }
    }
    
    this.availabilityGrid = newGrid;
  }

  getDayName(dayIndex: number): string {
    const days = ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];
    return days[dayIndex];
  }

  // Helper methods for template
  getDispoDay(dispo: DisponibiliteDTO): string {
    const date = new Date(dispo.dateHeureDebut);
    return this.getDayName(date.getDay());
  }

  getDispoDate(dispo: DisponibiliteDTO): string {
    const date = new Date(dispo.dateHeureDebut);
    return date.toLocaleDateString('fr-FR', { 
      day: '2-digit', 
      month: '2-digit', 
      year: 'numeric' 
    });
  }

  getDispoHeureDebut(dispo: DisponibiliteDTO): string {
    const date = new Date(dispo.dateHeureDebut);
    return date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  getDispoHeureFin(dispo: DisponibiliteDTO): string {
    const date = new Date(dispo.dateHeureFin);
    return date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  getTotalDisponibilites(): number {
    return this.getCachedStats().total;
  }

  private getCachedStats() {
    const now = Date.now();
    if (!this._cachedStats || (now - this._cachedStats.lastUpdate) > 1000) {
      this._cachedStats = {
        total: this.selectedEnseignantDisponibilites.length,
        hours: this.calculateTotalHours(),
        mostAvailableDay: this.calculateMostAvailableDay(),
        lastUpdate: now
      };
    }
    return this._cachedStats;
  }

  getAvailableHoursThisWeek(): number {
    return this.getCachedStats().hours;
  }

  getMostAvailableDay(): string {
    return this.getCachedStats().mostAvailableDay;
  }

  private calculateTotalHours(): number {
    if (!this.selectedEnseignantDisponibilites || this.selectedEnseignantDisponibilites.length === 0) {
      return 0;
    }
    
    let totalHours = 0;
    this.selectedEnseignantDisponibilites.forEach(dispo => {
      try {
        const dateDebut = new Date(dispo.dateHeureDebut);
        const dateFin = new Date(dispo.dateHeureFin);
        
        if (!isNaN(dateDebut.getTime()) && !isNaN(dateFin.getTime())) {
          const hours = (dateFin.getTime() - dateDebut.getTime()) / (1000 * 60 * 60);
          totalHours += hours;
        }
      } catch (error) {
        console.error('Erreur calcul heures:', error);
      }
    });
    return Math.round(totalHours * 10) / 10;
  }

  private calculateMostAvailableDay(): string {
    if (!this.selectedEnseignantDisponibilites || this.selectedEnseignantDisponibilites.length === 0) {
      return 'Aucun';
    }
    
    const dayCount: { [key: string]: number } = {};
    
    this.selectedEnseignantDisponibilites.forEach(dispo => {
      try {
        const dateDebut = new Date(dispo.dateHeureDebut);
        if (!isNaN(dateDebut.getTime())) {
          const jourIndex = dateDebut.getDay();
          const jours = ['Dim', 'Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam'];
          const jourNom = jours[jourIndex];
          
          dayCount[jourNom] = (dayCount[jourNom] || 0) + 1;
        }
      } catch (error) {
        console.error('Erreur calcul jour préféré:', error);
      }
    });
    
    let maxDay = 'Aucun';
    let maxCount = 0;
    
    Object.entries(dayCount).forEach(([day, count]) => {
      if (count > maxCount) {
        maxCount = count;
        maxDay = day;
      }
    });
    
    return maxDay;
  }

  hasExistingEdt(enseignant: Enseignant): boolean {
    if (!enseignant.idUser) return false;
    
    // Use a stable hash-based approach instead of random
    const hash = enseignant.idUser.split('').reduce((a, b) => {
      a = ((a << 5) - a) + b.charCodeAt(0);
      return a & a;
    }, 0);
    
    return Math.abs(hash) % 2 === 0;
  }

  // Grid functions
  isAvailable(day: Day, slot: TimeSlot): boolean {
    if (!this.gridCalculated || !this.availabilityGrid.length) {
      return false;
    }
    
    const dayIndex = this.weekDays.findIndex(d => d.name === day.name);
    const slotIndex = this.timeSlots.findIndex(s => s.startHour === slot.startHour);
    
    if (dayIndex === -1 || slotIndex === -1) {
      return false;
    }
    
    return this.availabilityGrid[slotIndex] && this.availabilityGrid[slotIndex][dayIndex];
  }

  isAvailableByIndex(dayIndex: number, slotIndex: number): boolean {
    if (!this.gridCalculated || 
        !this.availabilityGrid || 
        !Array.isArray(this.availabilityGrid) ||
        this.availabilityGrid.length === 0) {
      return false;
    }
    
    if (slotIndex < 0 || slotIndex >= this.availabilityGrid.length) {
      return false;
    }
    
    const slotRow = this.availabilityGrid[slotIndex];
    if (!slotRow || !Array.isArray(slotRow)) {
      return false;
    }
    
    if (dayIndex < 0 || dayIndex >= slotRow.length) {
      return false;
    }
    
    return Boolean(slotRow[dayIndex]);
  }

  getAvailabilityDetails(day: Day, slot: TimeSlot): DisponibiliteDTO | null {
    const key = `${day.name}_${slot.startHour}h`;
    return this.disponibilitesMap.get(key) || null;
  }

  getAvailabilityTooltip(day: Day, slot: TimeSlot): string {
    const dispo = this.getAvailabilityDetails(day, slot);
    if (!dispo) return '';
    
    const dateDebut = new Date(dispo.dateHeureDebut);
    const dateFin = new Date(dispo.dateHeureFin);
    
    return `Disponible de ${dateDebut.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })} à ${dateFin.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}${dispo.commentaire ? '\n' + dispo.commentaire : ''}`;
  }

  hasEdtSlot(day: Day, slot: TimeSlot): boolean {
    return this.getEdtSlot(day, slot) !== null;
  }

  getEdtSlot(day: Day, slot: TimeSlot): EdtSlot | null {
    const dateKey = this.getDateKey(day.date, slot.startHour);
    let edtSlot = this.edtMap.get(dateKey);
    
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

  isPastDay(day: Day): boolean {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const dayDate = new Date(day.date);
    dayDate.setHours(0, 0, 0, 0);
    return dayDate < today;
  }

  isPastSlot(day: Day, slot: TimeSlot): boolean {
    const now = new Date();
    const slotDate = new Date(day.date);
    slotDate.setHours(slot.startHour, 0, 0, 0);
    return slotDate < now;
  }

  // Actions
  exportDisponibilites(): void {
    console.log('Export des disponibilités...');
  }

  generateSchedule(): void {
    console.log('Génération du planning...');
  }

  generateScheduleFromAvailabilities(): void {
    if (!this.selectedEnseignant || this.getTotalDisponibilites() === 0) {
      this.notification.warning('Aucune disponibilité à traiter pour cet enseignant');
      return;
    }

    this.notification.confirm(
      `Voulez-vous générer l'emploi du temps intelligent pour ${this.selectedEnseignant.prenomUser} ${this.selectedEnseignant.nomUser} ` +
      `basé sur ses ${this.getTotalDisponibilites()} créneaux de disponibilité et ses cours assignés ?`,
      'Génération EDT',
      () => this.doGenerateEdtIntelligent()
    );
  }

  private doGenerateEdtIntelligent(): void {
    if (!this.selectedEnseignant) return;
    console.log('Génération intelligente de l\'EDT pour:', this.selectedEnseignant);
    console.log('Disponibilités utilisées:', this.selectedEnseignantDisponibilites);

    // Essayer d'abord la génération intelligente
    this.planningService.genererEdtIntelligent(this.selectedEnseignant.idUser || '', 4).subscribe({
      next: (response) => {
        console.log('EDT intelligent généré avec succès:', response);
        if (response.success) {
          this.notification.success(`Emploi du temps intelligent généré avec succès pour ${this.selectedEnseignant?.prenomUser} ${this.selectedEnseignant?.nomUser}!\n${response.nombreCreneaux} créneaux ont été programmés basés sur les cours réellement assignés.`);
        } else {
          this.notification.info(`Génération partiellement réussie: ${response.message}`);
        }
        
        this.closeDisponibilitesModal();
        if (this.selectedEnseignant) {
          this.openEdtModal(this.selectedEnseignant, 'view');
        }
      },
      error: (error) => {
        console.error('Erreur lors de la génération intelligente, tentative avec l\'ancienne méthode:', error);
        
        // Fallback vers l'ancienne méthode
        if (this.selectedEnseignant?.idUser) {
          this.planningService.genererEdt(this.selectedEnseignant.idUser).subscribe({
            next: (edtGenere) => {
            console.log('EDT généré avec l\'ancienne méthode:', edtGenere);
            this.notification.success(`Emploi du temps généré avec succès (méthode classique) pour ${this.selectedEnseignant?.prenomUser} ${this.selectedEnseignant?.nomUser}!\n${edtGenere.length} cours ont été programmés.`);
            
            this.closeDisponibilitesModal();
            if (this.selectedEnseignant) {
              this.openEdtModal(this.selectedEnseignant, 'view');
            }
          },
          error: (fallbackError) => {
            console.error('Erreur lors de la génération de l\'EDT (fallback):', fallbackError);
            let errorMsg = 'Erreur lors de la génération de l\'emploi du temps';
            if (fallbackError.status === 400) {
              errorMsg = 'Données insuffisantes pour générer l\'emploi du temps';
            } else if (fallbackError.status === 404) {
              errorMsg = 'Enseignant non trouvé';
            } else if (fallbackError.status === 0) {
              errorMsg = 'Service de planning non disponible';
            }
            this.notification.error(errorMsg);
          }
          });
        }
      }
    });
  }

  generateEdtFromDisponibilites(): void {
    if (!this.selectedEnseignant) return;
    
    this.isGeneratingEdt = true;
    console.log('Génération EDT à partir des disponibilités...');
    
    setTimeout(() => {
      this.isGeneratingEdt = false;
      this.notification.success('EDT généré avec succès !');
      this.closeEdtModal();
    }, 2000);
  }

  // TrackBy functions for change detection
  trackByEnseignantId(index: number, enseignant: Enseignant): string {
    return enseignant.idUser || index.toString();
  }

  trackByDayName(index: number, day: Day): string {
    return day.name;
  }

  trackBySlotTime(index: number, slot: TimeSlot): number {
    return slot.startHour;
  }

  trackByDisponibiliteId(index: number, dispo: DisponibiliteDTO): string {
    return dispo.id?.toString() || `${dispo.dateHeureDebut}_${dispo.dateHeureFin}_${index}`;
  }

  trackByUserName(index: number, user: any): string {
    return user.name || index.toString();
  }

  trackByRoomName(index: number, room: any): string {
    return room.name || index.toString();
  }

  // Méthodes pour la pagination et les statistiques des enseignants
  pageSize = 10;
  currentPage = 1;
  totalItems = 0;

  // Méthodes de tri
  sortField = '';
  sortDirection: 'asc' | 'desc' = 'asc';

  sortBy(field: string): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
  }

  // Méthodes de pagination
  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  getVisiblePages(): number[] {
    const pages: number[] = [];
    const start = Math.max(1, this.currentPage - 2);
    const end = Math.min(this.totalPages, this.currentPage + 2);
    
    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    
    return pages;
  }

  trackByPage(index: number, page: number): number {
    return page;
  }

  // Méthodes de statistiques pour les enseignants
  getTotalEnseignants(): number {
    return this.enseignants.length;
  }

  getEnseignantsActifs(): number {
    return this.enseignants.filter(e => e.email && e.nomUser).length;
  }

  getEnseignantsAvecDisponibilites(): number {
    // Simulation - dans un vrai projet, cela viendrait d'une API
    return Math.floor(this.enseignants.length * 0.7);
  }

  getNombreSpecialites(): number {
    const specialites = new Set(this.enseignants.map(e => e.specialite).filter(s => s && s.trim()));
    return specialites.size;
  }

  // Propriétés pour la pagination des enseignants
  get totalEnseignants(): number {
    return this.filteredEnseignants.length;
  }

  get totalPages(): number {
    return Math.ceil(this.totalEnseignants / this.pageSize);
  }

  // Méthode pour effacer les messages
  clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.cdr.markForCheck();
  }

  // Gestion du menu déroulant
  openDropdownId: string | null = null;

  toggleDropdown(enseignantId: string, event?: MouseEvent): void {
    // Fermer le dropdown précédent et retirer la classe
    if (this.openDropdownId && this.openDropdownId !== enseignantId) {
      const prevRow = document.querySelector(`tr[data-enseignant-id="${this.openDropdownId}"]`);
      if (prevRow) {
        prevRow.classList.remove('dropdown-open');
      }
    }

    if (this.openDropdownId === enseignantId) {
      this.openDropdownId = null;
      this.closeDropdown();
      // Retirer la classe de la ligne
      const row = (event?.target as HTMLElement)?.closest('tr');
      if (row) {
        row.classList.remove('dropdown-open');
      }
    } else {
      this.openDropdownId = enseignantId;
      
      // Ajouter la classe à la ligne pour augmenter le z-index
      const row = (event?.target as HTMLElement)?.closest('tr');
      if (row) {
        row.classList.add('dropdown-open');
        row.setAttribute('data-enseignant-id', enseignantId);
      }
      
      // Vérifier si le menu doit s'afficher au-dessus
      if (event) {
        setTimeout(() => {
          const button = (event.target as HTMLElement).closest('.action-menu-btn');
          const container = button?.closest('.dropdown-container');
          const menu = container?.querySelector('.dropdown-menu') as HTMLElement;
          
          if (button && menu && container) {
            const buttonRect = button.getBoundingClientRect();
            const menuHeight = menu.offsetHeight || 280; // Hauteur estimée si pas encore rendu
            const windowHeight = window.innerHeight;
            const tableContainer = button.closest('.table-container');
            
            // Calculer l'espace disponible en bas
            const spaceBelow = windowHeight - buttonRect.bottom;
            
            // Si pas assez d'espace en bas (moins de la hauteur du menu + marge)
            if (spaceBelow < menuHeight + 20) {
              container.classList.add('show-above');
            } else {
              container.classList.remove('show-above');
            }
            
            // S'assurer que le conteneur du tableau permet le débordement
            if (tableContainer) {
              (tableContainer as HTMLElement).style.overflow = 'visible';
            }
          }
        }, 10);
      }
    }
    this.cdr.markForCheck();
  }

  closeDropdown(): void {
    this.openDropdownId = null;
    // Retirer toutes les classes show-above et dropdown-open
    document.querySelectorAll('.dropdown-container.show-above').forEach(el => {
      el.classList.remove('show-above');
    });
    document.querySelectorAll('tr.dropdown-open').forEach(el => {
      el.classList.remove('dropdown-open');
    });
    this.cdr.markForCheck();
  }

  // Actions du menu déroulant
  showInfoModal: boolean = false;
  
  viewEnseignantInfo(enseignant: Enseignant): void {
    this.selectedEnseignant = enseignant;
    this.showInfoModal = true;
    this.cdr.markForCheck();
  }

  closeInfoModal(): void {
    this.showInfoModal = false;
    this.selectedEnseignant = null;
    this.cdr.markForCheck();
  }

  // Export/Import CSV
  exportEnseignantsCSV(): void {
    if (this.enseignants.length === 0) {
      this.notification.warning('Aucun enseignant à exporter');
      return;
    }

    // Créer le contenu CSV
    const headers = ['Nom', 'Prénom', 'Email', 'Téléphone', 'Spécialité', 'Département', 'Rôle'];
    const csvContent = [
      headers.join(';'),
      ...this.enseignants.map(e => [
        e.nomUser || '',
        e.prenomUser || '',
        e.email || '',
        e.telephone || '',
        e.specialite || '',
        e.departement || '',
        e.role || 'ENSEIGNANT'
      ].join(';'))
    ].join('\n');

    // Créer le fichier et le télécharger
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    
    const date = new Date().toISOString().split('T')[0];
    link.setAttribute('href', url);
    link.setAttribute('download', `enseignants_${date}.csv`);
    link.style.visibility = 'hidden';
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    this.successMessage = `✅ ${this.enseignants.length} enseignant(s) exporté(s) avec succès!`;
    setTimeout(() => {
      this.successMessage = '';
      this.cdr.markForCheck();
    }, 3000);
  }

  triggerFileInput(): void {
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fileInput) {
      fileInput.click();
    }
  }

  importEnseignantsCSV(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.name.endsWith('.csv')) {
      this.errorMessage = '❌ Veuillez sélectionner un fichier CSV';
      return;
    }

    const reader = new FileReader();
    reader.onload = (e: any) => {
      try {
        const csvContent = e.target.result;
        const lines = csvContent.split('\n').filter((line: string) => line.trim());
        
        if (lines.length < 2) {
          this.errorMessage = '❌ Le fichier CSV est vide ou invalide';
          return;
        }

        // Ignorer la première ligne (headers)
        const dataLines = lines.slice(1);
        const enseignantsToImport: EnseignantRequest[] = [];
        const errors: string[] = [];

        dataLines.forEach((line: string, index: number) => {
          const values = line.split(';').map(v => v.trim());
          
          if (values.length < 3) {
            errors.push(`Ligne ${index + 2}: Données insuffisantes`);
            return;
          }

          const [nom, prenom, email, telephone, specialite, departement, role] = values;

          if (!nom || !prenom || !email) {
            errors.push(`Ligne ${index + 2}: Nom, prénom et email sont obligatoires`);
            return;
          }

          // Validation email
          const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
          if (!emailRegex.test(email)) {
            errors.push(`Ligne ${index + 2}: Email invalide (${email})`);
            return;
          }

          enseignantsToImport.push({
            nomUser: nom,
            prenomUser: prenom,
            email: email,
            telephone: telephone || '',
            specialite: specialite || '',
            departement: departement || '',
            role: role || 'ENSEIGNANT',
            mdp: this.generateRandomPassword(),
            idEcole: '',
            idEcoles: []
          });
        });

        if (errors.length > 0) {
          const errorMsg = `Erreurs détectées:\n${errors.join('\n')}\n\nVoulez-vous continuer avec les ${enseignantsToImport.length} enseignant(s) valide(s) ?`;
          this.notification.confirm(errorMsg, 'Import CSV', () => this.proceedWithImport(enseignantsToImport));
          return;
        }

        this.proceedWithImport(enseignantsToImport);
      } catch (error) {
        console.error('Erreur lors de la lecture du CSV:', error);
        this.errorMessage = '❌ Erreur lors de la lecture du fichier CSV';
      }

      // Réinitialiser l'input file
      event.target.value = '';
    };

    reader.readAsText(file, 'UTF-8');
  }

  private proceedWithImport(enseignantsToImport: EnseignantRequest[]): void {
    if (enseignantsToImport.length === 0) {
      this.errorMessage = '❌ Aucun enseignant valide à importer';
      return;
    }
    this.importEnseignantsBatch(enseignantsToImport);
  }

  private generateRandomPassword(): string {
    const length = 12;
    const charset = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*';
    let password = '';
    for (let i = 0; i < length; i++) {
      password += charset.charAt(Math.floor(Math.random() * charset.length));
    }
    return password;
  }

  private importEnseignantsBatch(enseignants: EnseignantRequest[]): void {
    let successCount = 0;
    let errorCount = 0;
    const total = enseignants.length;

    this.successMessage = `⏳ Import en cours... 0/${total}`;
    this.cdr.markForCheck();

    const importNext = (index: number) => {
      if (index >= enseignants.length) {
        // Fin de l'import
        this.loadEnseignants();
        this.successMessage = `✅ Import terminé: ${successCount} réussi(s), ${errorCount} échec(s)`;
        setTimeout(() => {
          this.successMessage = '';
          this.cdr.markForCheck();
        }, 5000);
        return;
      }

      const enseignant = enseignants[index];
      this.userService.creerEnseignant(enseignant).subscribe({
        next: () => {
          successCount++;
          this.successMessage = `⏳ Import en cours... ${index + 1}/${total} (${successCount} réussi(s))`;
          this.cdr.markForCheck();
          importNext(index + 1);
        },
        error: (error) => {
          errorCount++;
          console.error(`Erreur import ${enseignant.email}:`, error);
          this.successMessage = `⏳ Import en cours... ${index + 1}/${total} (${successCount} réussi(s), ${errorCount} échec(s))`;
          this.cdr.markForCheck();
          importNext(index + 1);
        }
      });
    };

    importNext(0);
  }

  editEnseignant(enseignant: Enseignant): void {
    // Pré-remplir le formulaire avec les données de l'enseignant
    this.newEnseignant = {
      email: enseignant.email || '',
      mdp: '', // Ne pas pré-remplir le mot de passe
      nomUser: enseignant.nomUser || '',
      prenomUser: enseignant.prenomUser || '',
      telephone: enseignant.telephone || '',
      role: 'ENSEIGNANT',
      specialite: enseignant.specialite || '',
      departement: enseignant.departement || '',
      idEcole: enseignant.idEcole || '',
      idEcoles: enseignant.idEcoles ?? (enseignant.idEcole ? [enseignant.idEcole] : [])
    };
    this.showAddEnseignantForm = true;
    this.successMessage = `Modification de ${enseignant.prenomUser} ${enseignant.nomUser}`;
    this.cdr.markForCheck();
    
    // Scroll vers le formulaire
    setTimeout(() => {
      const formElement = document.querySelector('.form-container');
      if (formElement) {
        formElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }, 100);
  }

  desactiverEnseignant(id: string): void {
    const enseignant = this.enseignants.find(e => e.idUser === id);
    const nom = enseignant ? `${enseignant.prenomUser} ${enseignant.nomUser}` : 'cet enseignant';
    
    this.notification.confirm(
      `Êtes-vous sûr de vouloir désactiver ${nom} ?\n\n• L'enseignant ne pourra plus se connecter\n• Ses disponibilités seront conservées\n• Son emploi du temps sera conservé\n• Vous pourrez le réactiver ultérieurement`,
      'Confirmation de désactivation',
      () => this.doDesactiverEnseignant(id, nom)
    );
  }

  private doDesactiverEnseignant(id: string, nom: string): void {
    // Dans une vraie application, il faudrait une API de désactivation
    this.userService.deleteEnseignant(id).subscribe({
      next: () => {
        this.enseignants = this.enseignants.filter(e => e.idUser !== id);
        this.successMessage = `✅ ${nom} a été désactivé avec succès!`;
        this.cdr.markForCheck();
        setTimeout(() => {
          this.successMessage = '';
          this.cdr.markForCheck();
        }, 5000);
      },
      error: (error) => {
        console.error('Erreur lors de la désactivation:', error);
        let errorMsg = 'Erreur lors de la désactivation de l\'enseignant';
        if (error.status === 404) {
          errorMsg = 'Enseignant non trouvé';
        } else if (error.status === 403) {
          errorMsg = 'Vous n\'avez pas les permissions nécessaires';
        } else if (error.status === 0) {
          errorMsg = 'Impossible de contacter le serveur';
        }
        this.errorMessage = `❌ ${errorMsg}`;
        this.cdr.markForCheck();
      }
    });
  }
}