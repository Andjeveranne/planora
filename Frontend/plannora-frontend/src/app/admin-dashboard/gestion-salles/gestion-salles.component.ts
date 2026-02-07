import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { SalleService, SalleDTO, TypeSalle, StatutSalle } from '../../services/salle.service';
import { AuthService } from '../../services/auth.service';
import { TestAuthService } from '../../services/test-auth.service';
import { NotificationService } from '../../services/notification.service';

interface Activity {
  id: number;
  message: string;
  type: 'success' | 'warning' | 'error';
}

@Component({
  selector: 'app-gestion-salles',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './gestion-salles.component.html',
  styleUrls: ['./gestion-salles.component.css']
})
export class GestionSallesComponent implements OnInit {
  salles: SalleDTO[] = [];
  salleForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  showModal = false;
  isEditMode = false;
  currentSalleId = '';
  
  // Filtres
  filtreType: TypeSalle | '' = '';
  filtreStatut: StatutSalle | '' = '';
  filtreRecherche = '';
  selectedSchool = 'SAINT_JEAN_INGENIEUR';

  // Pagination
  currentPage = 1;
  pageSize = 10;
  totalItems = 0;
  totalPages = 0;

  // Tri
  sortField = '';
  sortDirection: 'asc' | 'desc' = 'asc';

  // Activités récentes
  recentActivities: Activity[] = [
    { id: 1, message: 'Salle 305 Bâtiment Secondaire R3', type: 'success' },
    { id: 2, message: 'Salle 305 Bâtiment Secondaire R3', type: 'warning' },
    { id: 3, message: 'Salle 305 Bâtiment Secondaire R3', type: 'error' }
  ];

  // Enums pour les templates
  TypeSalle = TypeSalle;
  StatutSalle = StatutSalle;

  constructor(
    private salleService: SalleService,
    private authService: AuthService,
    private fb: FormBuilder,
    private testAuthService: TestAuthService,
    private notification: NotificationService
  ) {
    this.salleForm = this.fb.group({
      nomSalle: ['', [Validators.required, Validators.minLength(2)]],
      type: ['', Validators.required],
      localisation: ['', [Validators.required, Validators.minLength(2)]],
      capacite: ['', [Validators.required, Validators.min(1)]],
      capaciteExam: ['', [Validators.required, Validators.min(1)]],
      idEcole: ['SAINT_JEAN_INGENIEUR', Validators.required],
      statut: [StatutSalle.DISPONIBLE, Validators.required]
    });
  }

  ngOnInit(): void {
    // Debug de l'authentification
    console.log('🔍 Debug Auth - Token Info:', this.testAuthService.checkToken());
    
    // Test des endpoints
    console.log('🔍 Testing health endpoint...');
    this.testAuthService.testHealth().subscribe({
      next: (result) => console.log('✅ Health OK:', result),
      error: (error) => console.error('❌ Health Error:', error)
    });

    console.log('🔍 Testing auth endpoint...');
    this.testAuthService.testAuth().subscribe({
      next: (result) => console.log('✅ Auth Debug OK:', result),
      error: (error) => console.error('❌ Auth Debug Error:', error)
    });

    this.chargerSalles();
  }

  chargerSalles(): void {
    this.isLoading = true;
    this.salleService.obtenirToutesLesSalles().subscribe({
      next: (salles) => {
        this.salles = salles;
        this.updatePagination();
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Erreur lors du chargement des salles';
        this.isLoading = false;
        console.error('Erreur:', error);
      }
    });
  }

  get sallesFiltrees(): SalleDTO[] {
    let filtered = this.salles.filter(salle => {
      const matchType = !this.filtreType || salle.type === this.filtreType;
      const matchStatut = !this.filtreStatut || salle.statut === this.filtreStatut;
      const matchRecherche = !this.filtreRecherche || 
        salle.nomSalle.toLowerCase().includes(this.filtreRecherche.toLowerCase()) ||
        salle.localisation.toLowerCase().includes(this.filtreRecherche.toLowerCase());
      
      return matchType && matchStatut && matchRecherche;
    });

    // Tri
    if (this.sortField) {
      filtered.sort((a, b) => {
        const aValue = (a as any)[this.sortField];
        const bValue = (b as any)[this.sortField];
        const comparison = aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
        return this.sortDirection === 'asc' ? comparison : -comparison;
      });
    }

    // Pagination
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    
    this.totalItems = filtered.length;
    this.totalPages = Math.ceil(this.totalItems / this.pageSize);
    
    return filtered.slice(startIndex, endIndex);
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
  updatePagination(): void {
    this.totalItems = this.salles.length;
    this.totalPages = Math.ceil(this.totalItems / this.pageSize);
  }

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

  // Méthodes de statistiques
  getTotalSalles(): number {
    return this.salles.length;
  }

  getSallesLibres(): number {
    return this.salles.filter(s => s.statut === StatutSalle.DISPONIBLE).length;
  }

  getSallesOccupees(): number {
    return this.salles.filter(s => s.statut === StatutSalle.OCCUPEE).length;
  }

  getSallesEnMaintenance(): number {
    return this.salles.filter(s => s.statut === StatutSalle.MAINTENANCE).length;
  }

  // Méthodes de style pour les statuts
  getStatusClass(statut: StatutSalle): string {
    switch (statut) {
      case StatutSalle.DISPONIBLE:
        return 'status-libre';
      case StatutSalle.OCCUPEE:
        return 'status-occupe';
      case StatutSalle.MAINTENANCE:
        return 'status-maintenance';
      case StatutSalle.HORS_SERVICE:
        return 'status-inactive';
      default:
        return 'status-inactive';
    }
  }

  ouvrirModal(salle?: SalleDTO): void {
    this.showModal = true;
    this.isEditMode = !!salle;
    this.errorMessage = '';
    this.successMessage = '';

    if (salle) {
      this.currentSalleId = salle.idSalle!;
      this.salleForm.patchValue(salle);
    } else {
      this.salleForm.reset();
      this.salleForm.patchValue({
        idEcole: 'SAINT_JEAN_INGENIEUR',
        statut: StatutSalle.DISPONIBLE
      });
    }
  }

  fermerModal(): void {
    this.showModal = false;
    this.salleForm.reset();
    this.currentSalleId = '';
  }

  onSubmit(): void {
    if (this.salleForm.valid) {
      this.isLoading = true;
      const salleData: SalleDTO = this.salleForm.value;

      const operation = this.isEditMode 
        ? this.salleService.mettreAJourSalle(this.currentSalleId, salleData)
        : this.salleService.creerSalle(salleData);

      operation.subscribe({
        next: (salle) => {
          this.successMessage = this.isEditMode 
            ? 'Salle mise à jour avec succès' 
            : 'Salle créée avec succès';
          this.chargerSalles();
          this.fermerModal();
          this.isLoading = false;
        },
        error: (error) => {
          this.errorMessage = error.error?.message || 'Erreur lors de l\'opération';
          this.isLoading = false;
          console.error('Erreur:', error);
        }
      });
    } else {
      this.salleForm.markAllAsTouched();
    }
  }

  changerStatut(salle: SalleDTO, nouveauStatut: StatutSalle): void {
    this.salleService.changerStatutSalle(salle.idSalle!, nouveauStatut).subscribe({
      next: () => {
        this.successMessage = 'Statut mis à jour avec succès';
        this.chargerSalles();
      },
      error: (error) => {
        this.errorMessage = 'Erreur lors du changement de statut';
        console.error('Erreur:', error);
      }
    });
  }

  supprimerSalle(salle: SalleDTO): void {
    this.notification.confirm(
      `Êtes-vous sûr de vouloir supprimer la salle "${salle.nomSalle}" ?`,
      'Confirmation de suppression',
      () => {
        this.salleService.supprimerSalle(salle.idSalle!).subscribe({
          next: () => {
            this.successMessage = 'Salle supprimée avec succès';
            this.chargerSalles();
          },
          error: (error) => {
            this.errorMessage = 'Erreur lors de la suppression';
            console.error('Erreur:', error);
          }
        });
      }
    );
  }

  getTypeLabel(type: TypeSalle): string {
    return this.salleService.getTypeLabel(type);
  }

  getStatutLabel(statut: StatutSalle): string {
    return this.salleService.getStatutLabel(statut);
  }

  getStatutColor(statut: StatutSalle): string {
    return this.salleService.getStatutColor(statut);
  }

  clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  // Getters pour la validation des formulaires
  get nomSalle() { return this.salleForm.get('nomSalle'); }
  get type() { return this.salleForm.get('type'); }
  get localisation() { return this.salleForm.get('localisation'); }
  get capacite() { return this.salleForm.get('capacite'); }
  get capaciteExam() { return this.salleForm.get('capaciteExam'); }
}