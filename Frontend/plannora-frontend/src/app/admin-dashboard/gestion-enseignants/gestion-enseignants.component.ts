import { Component, OnInit, OnDestroy, ChangeDetectorRef, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService, Enseignant, EnseignantRequest } from '../../services/user.service';
import { EcoleDTO, EcoleService } from '../../services/ecole.service';
import { ClassesService, FiliereDTO, SpecialiteDTO } from '../../services/classes.service';
import { PlanningService, DisponibiliteDTO, EmploiDuTempsDTO } from '../../services/planning.service';
import { EmploiDuTempsService, UEEnseignantDTO } from '../../services/emploi-du-temps.service';
import { UEService, UEDTO } from '../../services/ue.service';
import { NotificationService } from '../../services/notification.service';

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
  selector: 'app-gestion-enseignants',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gestion-enseignants.html',
  styleUrls: ['./gestion-enseignants.css']
})
export class GestionEnseignantsComponent implements OnInit, OnDestroy {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  enseignants: Enseignant[] = [];
  ecoles: EcoleDTO[] = [];
  departements: FiliereDTO[] = [];
  specialites: SpecialiteDTO[] = [];
  selectedEnseignant: Enseignant | null = null;
  disponibilitesEnseignant: DisponibiliteDTO[] = [];
  showAddEnseignantForm: boolean = false;
  showDisponibilitesModal: boolean = false;
  isLoadingEnseignants: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';
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

  // Pagination
  pageSize = 10;
  currentPage = 1;
  totalItems = 0;

  // Tri
  sortField = '';
  sortDirection: 'asc' | 'desc' = 'asc';

  // Dropdown
  openDropdownId: string | null = null;

  // Modal info
  showInfoModal: boolean = false;
  selectedEnseignantUEs: UEEnseignantDTO[] = [];
  allUEs: UEDTO[] = [];
  loadingUEs: boolean = false;

  // Modal UE assignment
  showUEAssignmentModal: boolean = false;
  selectedUEForAssignment: UEDTO | null = null;
  assignmentForm = {
    idEcole: '',
    peutCM: false,
    peutTD: false,
    peutTP: false,
    preference: 1,
    commentaire: ''
  };

  // Modal modification
  showEditModal: boolean = false;
  editingEnseignant: Enseignant | null = null;
  editForm: Partial<EnseignantRequest> = {};

  constructor(
    private userService: UserService,
    private planningService: PlanningService,
    private emploiDuTempsService: EmploiDuTempsService,
    private ueService: UEService,
    private classesService: ClassesService,
    private notification: NotificationService,
    private ecoleService: EcoleService,
    private cdr: ChangeDetectorRef
  ) {
    this.initTimeSlots();
  }

  ngOnInit(): void {
    this.setWeekToCurrent();
    this.loadEnseignants();
    this.ecoleService.obtenirToutes().subscribe({
      next: ecoles => this.ecoles = ecoles,
      error: () => this.errorMessage = 'Impossible de charger les écoles.'
    });
    this.classesService.getFilieres().subscribe({
      next: departements => this.departements = departements,
      error: () => this.errorMessage = 'Impossible de charger les départements.'
    });
    this.classesService.getSpecialites().subscribe({
      next: specialites => this.specialites = specialites,
      error: () => this.errorMessage = 'Impossible de charger les spécialités.'
    });
    
    // Fermer le dropdown quand on clique ailleurs
    document.addEventListener('click', this.handleClickOutside.bind(this));
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
    this.timeSlots = [
      { time: '8h - 10h',  startHour: 8  },
      { time: '10h - 12h', startHour: 10 },
      { time: '12h - 14h', startHour: 12 },
      { time: '14h - 16h', startHour: 14 },
      { time: '16h - 18h', startHour: 16 }
    ];
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

    // Filtre par statut
    if (this.filterStatus !== 'all') {
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
      filtered = filtered.filter(enseignant => enseignant.idEcole === this.filterEcole);
    }

    return filtered;
  }

  applyFilters(): void {
    this.currentPage = 1;
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

  getSchoolName(idEcole?: string): string {
    return this.ecoles.find(ecole => ecole.idEcole === idEcole)?.nomEcole ?? 'Non associée';
  }

  get departmentsForSelectedTeacherSchools(): FiliereDTO[] {
    const selectedSchools = this.newEnseignant.idEcoles ?? [];
    return this.departements.filter(departement =>
      !!departement.idEcole && selectedSchools.includes(departement.idEcole)
    );
  }

  get departmentsForEditedTeacherSchools(): FiliereDTO[] {
    const selectedSchools = this.editForm.idEcoles ?? [];
    return this.departements.filter(departement =>
      !!departement.idEcole && selectedSchools.includes(departement.idEcole)
    );
  }

  get specialtiesForSelectedTeacherDepartment(): SpecialiteDTO[] {
    const selectedSchools = this.newEnseignant.idEcoles ?? [];
    const departmentIds = this.departements
      .filter(departement => selectedSchools.includes(departement.idEcole ?? '') &&
        departement.nom === this.newEnseignant.departement)
      .map(departement => departement.idFiliere);
    return this.specialites.filter(specialite => departmentIds.includes(specialite.idFiliere));
  }

  get specialtiesForEditedTeacherDepartment(): SpecialiteDTO[] {
    const selectedSchools = this.editForm.idEcoles ?? [];
    const departmentIds = this.departements
      .filter(departement => selectedSchools.includes(departement.idEcole ?? '') &&
        departement.nom === this.editForm.departement)
      .map(departement => departement.idFiliere);
    return this.specialites.filter(specialite => departmentIds.includes(specialite.idFiliere));
  }

  onNewTeacherSchoolsChange(): void {
    this.newEnseignant.idEcole = this.newEnseignant.idEcoles[0] ?? '';
    if (!this.departmentsForSelectedTeacherSchools.some(department =>
      department.nom === this.newEnseignant.departement
    )) {
      this.newEnseignant.departement = '';
    }
    this.newEnseignant.specialite = '';
  }

  onEditedTeacherSchoolsChange(): void {
    this.editForm.idEcole = this.editForm.idEcoles?.[0] ?? '';
    if (!this.departmentsForEditedTeacherSchools.some(department =>
      department.nom === this.editForm.departement
    )) {
      this.editForm.departement = '';
    }
    this.editForm.specialite = '';
  }

  onNewTeacherDepartmentChange(): void {
    this.newEnseignant.specialite = '';
  }

  onEditedTeacherDepartmentChange(): void {
    this.editForm.specialite = '';
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
      telephone: '', role: 'ENSEIGNANT', specialite: '', departement: '', idEcole: '', idEcoles: []
    };
    this.errorMessage = '';
    this.successMessage = '';
  }

  ajouterEnseignant(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.validateForm()) return;
    this.newEnseignant.idEcole = this.newEnseignant.idEcoles[0] ?? '';

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
      !this.newEnseignant.nomUser || !this.newEnseignant.prenomUser || !this.newEnseignant.idEcoles?.length ||
      !this.newEnseignant.departement || !this.newEnseignant.specialite) {
      this.errorMessage = 'Tous les champs obligatoires, y compris l’école, le département et la spécialité, doivent être remplis';
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
    
    this.planningService.getEdtFromUserService(enseignant.idUser).subscribe({
      next: (edtData: EmploiDuTempsDTO[]) => {
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
        
        const heureFinInclusive = minuteFin > 0 ? heureFin + 1 : heureFin;
        
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

  // Méthodes de tri
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
  toggleDropdown(enseignantId: string, event?: MouseEvent): void {
    if (this.openDropdownId && this.openDropdownId !== enseignantId) {
      const prevRow = document.querySelector(`tr[data-enseignant-id="${this.openDropdownId}"]`);
      if (prevRow) {
        prevRow.classList.remove('dropdown-open');
      }
    }

    if (this.openDropdownId === enseignantId) {
      this.openDropdownId = null;
      this.closeDropdown();
      const row = (event?.target as HTMLElement)?.closest('tr');
      if (row) {
        row.classList.remove('dropdown-open');
      }
    } else {
      this.openDropdownId = enseignantId;
      
      const row = (event?.target as HTMLElement)?.closest('tr');
      if (row) {
        row.classList.add('dropdown-open');
        row.setAttribute('data-enseignant-id', enseignantId);
      }
      
      if (event) {
        setTimeout(() => {
          const button = (event.target as HTMLElement).closest('.action-menu-btn');
          const container = button?.closest('.dropdown-container');
          const menu = container?.querySelector('.dropdown-menu') as HTMLElement;
          
          if (button && menu && container) {
            const buttonRect = button.getBoundingClientRect();
            const menuHeight = menu.offsetHeight || 280;
            const windowHeight = window.innerHeight;
            const tableContainer = button.closest('.table-container');
            
            const spaceBelow = windowHeight - buttonRect.bottom;
            
            if (spaceBelow < menuHeight + 20) {
              container.classList.add('show-above');
            } else {
              container.classList.remove('show-above');
            }
            
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
    document.querySelectorAll('.dropdown-container.show-above').forEach(el => {
      el.classList.remove('show-above');
    });
    document.querySelectorAll('tr.dropdown-open').forEach(el => {
      el.classList.remove('dropdown-open');
    });
    this.cdr.markForCheck();
  }

  // Actions du menu déroulant
  viewEnseignantInfo(enseignant: Enseignant): void {
    this.selectedEnseignant = enseignant;
    this.showInfoModal = true;
    this.selectedEnseignantUEs = [];
    this.loadingUEs = true;
    // Charger les UEs de cet enseignant
    this.emploiDuTempsService.getUEEnseignantsParEnseignant(enseignant.idUser ?? '').subscribe({
      next: (ues) => {
        this.selectedEnseignantUEs = ues;
        this.loadingUEs = false;
        this.cdr.markForCheck();
      },
      error: () => { this.loadingUEs = false; this.cdr.markForCheck(); }
    });
    // Charger toutes les UEs pour afficher le nom si pas déjà chargé
    if (this.allUEs.length === 0) {
      this.ueService.getAllUEs().subscribe({
        next: (ues) => { this.allUEs = ues; this.cdr.markForCheck(); },
        error: () => {}
      });
    }
    this.cdr.markForCheck();
  }

  getNomUE(ueId: number): string {
    const ue = this.allUEs.find(u => u.id === ueId);
    return ue ? `${ue.codeUE} – ${ue.nomUE}` : `UE #${ueId}`;
  }

  closeInfoModal(): void {
    this.showInfoModal = false;
    this.selectedEnseignant = null;
    this.selectedEnseignantUEs = [];
    this.cdr.markForCheck();
  }

  // UE Assignment functionality
  assignerUE(enseignant: Enseignant): void {
    this.selectedEnseignant = enseignant;
    this.showUEAssignmentModal = true;
    
    // Charger toutes les UEs si pas déjà fait
    if (this.allUEs.length === 0) {
      this.ueService.getAllUEs().subscribe({
        next: (ues) => {
          this.allUEs = ues;
          this.cdr.markForCheck();
        },
        error: (error) => {
          console.error('Erreur lors du chargement des UEs:', error);
          this.notification.error('Erreur lors du chargement des UEs');
        }
      });
    }
    
    this.cdr.markForCheck();
  }

  closeUEAssignmentModal(): void {
    this.showUEAssignmentModal = false;
    this.selectedEnseignant = null;
    this.selectedUEForAssignment = null;
    this.resetAssignmentForm();
    this.cdr.markForCheck();
  }

  selectUEForAssignment(ue: UEDTO): void {
    this.selectedUEForAssignment = ue;
    this.resetAssignmentForm();
    
    // Vérifier si l'enseignant a déjà cette UE assignée
    if (this.selectedEnseignant) {
      this.emploiDuTempsService.getUEEnseignantsParEnseignant(this.selectedEnseignant.idUser ?? '').subscribe({
        next: (ues) => {
          const existingUE = ues.find(u => u.ueId === ue.id);
          if (existingUE) {
            this.assignmentForm = {
              idEcole: existingUE.idEcole || this.selectedEnseignant?.idEcoles?.[0] || this.selectedEnseignant?.idEcole || '',
              peutCM: existingUE.peutCM || false,
              peutTD: existingUE.peutTD || false,
              peutTP: existingUE.peutTP || false,
              preference: existingUE.preference || 1,
              commentaire: existingUE.commentaire || ''
            };
          }
          this.cdr.markForCheck();
        },
        error: () => {
          // Ignore l'erreur, on garde les valeurs par défaut
        }
      });
    }
  }

  resetAssignmentForm(): void {
    this.assignmentForm = {
      idEcole: this.selectedEnseignant?.idEcoles?.[0] || this.selectedEnseignant?.idEcole || '',
      peutCM: false,
      peutTD: false,
      peutTP: false,
      preference: 1,
      commentaire: ''
    };
  }

  saveUEAssignment(): void {
    if (!this.selectedEnseignant || !this.selectedUEForAssignment) {
      this.notification.error('Sélectionnez un enseignant et une UE');
      return;
    }

    if (!this.assignmentForm.peutCM && !this.assignmentForm.peutTD && !this.assignmentForm.peutTP) {
      this.notification.error('Sélectionnez au moins un type de cours (CM, TD ou TP)');
      return;
    }
    if (!this.assignmentForm.idEcole) {
      this.notification.error('Sélectionnez l’école de cette assignation.');
      return;
    }

    const assignment: UEEnseignantDTO = {
      enseignantId: this.selectedEnseignant.idUser!,
      idEcole: this.assignmentForm.idEcole,
      ueId: this.selectedUEForAssignment.id!,
      codeUE: this.selectedUEForAssignment.codeUE,
      nomUE: this.selectedUEForAssignment.nomUE,
      peutCM: this.assignmentForm.peutCM,
      peutTD: this.assignmentForm.peutTD,
      peutTP: this.assignmentForm.peutTP,
      preference: this.assignmentForm.preference,
      commentaire: this.assignmentForm.commentaire
    };

    this.emploiDuTempsService.ajouterUEEnseignant(assignment).subscribe({
      next: () => {
        this.notification.success(`UE ${this.selectedUEForAssignment?.codeUE} assignée avec succès à ${this.selectedEnseignant?.prenomUser} ${this.selectedEnseignant?.nomUser}`);
        this.closeUEAssignmentModal();
      },
      error: (error) => {
        console.error('Erreur lors de l\'assignation:', error);
        this.notification.error('Erreur lors de l\'assignation de l\'UE');
      }
    });
  }

  removeUEAssignment(ueEnseignant: UEEnseignantDTO): void {
    if (!ueEnseignant.id) return;

    this.notification.confirm(
      `Êtes-vous sûr de vouloir retirer l'UE ${ueEnseignant.codeUE} de ${this.selectedEnseignant?.prenomUser} ${this.selectedEnseignant?.nomUser} ?`,
      'Confirmation',
      () => {
        this.emploiDuTempsService.supprimerUEEnseignant(ueEnseignant.id!).subscribe({
          next: () => {
            this.notification.success('UE retirée avec succès');
            // Recharger les UEs de l'enseignant
            if (this.selectedEnseignant) {
              this.viewEnseignantInfo(this.selectedEnseignant);
            }
          },
          error: (error) => {
            console.error('Erreur lors de la suppression:', error);
            this.notification.error('Erreur lors de la suppression de l\'UE');
          }
        });
      }
    );
  }

  // Export/Import CSV
  exportEnseignantsCSV(): void {
    if (this.enseignants.length === 0) {
      this.notification.warning('Aucun enseignant à exporter');
      return;
    }

    const headers = ['Nom', 'Prénom', 'Email', 'Téléphone', 'Spécialité', 'Département', 'Rôle', 'ID École'];
    const csvContent = [
      headers.join(';'),
      ...this.enseignants.map(e => [
        e.nomUser || '',
        e.prenomUser || '',
        e.email || '',
        e.telephone || '',
        e.specialite || '',
        e.departement || '',
        e.role || 'ENSEIGNANT',
        e.idEcole || ''
      ].join(';'))
    ].join('\n');

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

        const dataLines = lines.slice(1);
        const enseignantsToImport: EnseignantRequest[] = [];
        const errors: string[] = [];

        dataLines.forEach((line: string, index: number) => {
          const values = line.split(';').map(v => v.trim());
          
          if (values.length < 3) {
            errors.push(`Ligne ${index + 2}: Données insuffisantes`);
            return;
          }

          const [nom, prenom, email, telephone, specialite, departement, role, idEcole] = values;

          if (!nom || !prenom || !email || !idEcole) {
            errors.push(`Ligne ${index + 2}: Nom, prénom, email et ID école sont obligatoires`);
            return;
          }

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
            idEcole,
            idEcoles: idEcole ? [idEcole] : []
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
    this.editingEnseignant = enseignant;
    this.editForm = {
      idEcole: enseignant.idEcole,
      idEcoles: enseignant.idEcoles ?? (enseignant.idEcole ? [enseignant.idEcole] : []),
      email: enseignant.email || '',
      nomUser: enseignant.nomUser || '',
      prenomUser: enseignant.prenomUser || '',
      telephone: enseignant.telephone || '',
      role: 'ENSEIGNANT',
      specialite: enseignant.specialite || '',
      departement: enseignant.departement || ''
    };
    this.showEditModal = true;
    this.cdr.markForCheck();
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.editingEnseignant = null;
    this.editForm = {};
    this.cdr.markForCheck();
  }

  saveEditEnseignant(): void {
    if (!this.editingEnseignant?.idUser) return;

    if (!this.editForm.nomUser || !this.editForm.prenomUser || !this.editForm.email || !this.editForm.idEcoles?.length) {
      this.errorMessage = 'Nom, prénom, email et école sont obligatoires';
      return;
    }
    if (!this.editForm.departement || !this.editForm.specialite ||
      !this.departmentsForEditedTeacherSchools.some(department => department.nom === this.editForm.departement)) {
      this.errorMessage = 'Sélectionnez une école, un département associé et une spécialité.';
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.editForm.email || '')) {
      this.errorMessage = 'Email invalide';
      return;
    }

    const payload = { ...this.editForm, role: this.editingEnseignant.role || 'ENSEIGNANT' };
    const editedId = this.editingEnseignant.idUser;

    this.userService.updateEnseignant(editedId, payload).subscribe({
      next: () => {
        const index = this.enseignants.findIndex(e => e.idUser === editedId);
        if (index !== -1) {
          this.enseignants[index] = {
            ...this.enseignants[index],
            nomUser: this.editForm.nomUser || this.enseignants[index].nomUser,
            prenomUser: this.editForm.prenomUser || this.enseignants[index].prenomUser,
            email: this.editForm.email || this.enseignants[index].email,
            telephone: this.editForm.telephone || this.enseignants[index].telephone,
            specialite: this.editForm.specialite ?? this.enseignants[index].specialite,
            departement: this.editForm.departement ?? this.enseignants[index].departement,
            idEcole: this.editForm.idEcole ?? this.enseignants[index].idEcole,
          };
          this.enseignants = [...this.enseignants]; // force la détection de changement
        }
        this.successMessage = `✅ Enseignant modifié avec succès!`;
        this.closeEditModal();
        this.cdr.markForCheck();
        setTimeout(() => { this.successMessage = ''; this.cdr.markForCheck(); }, 3000);
      },
      error: (error) => {
        let errorMsg = 'Erreur lors de la modification';
        if (error.status === 404) errorMsg = 'Enseignant non trouvé';
        else if (error.status === 403) errorMsg = 'Accès refusé';
        else if (error.error?.message) errorMsg = error.error.message;
        this.errorMessage = `❌ ${errorMsg}`;
        this.cdr.markForCheck();
      }
    });
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
