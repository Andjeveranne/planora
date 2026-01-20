import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UEService, UEDTO, UEStatistics, UEOptions } from '../services/ue.service';
import { UserService } from '../services/user.service';

@Component({
  selector: 'app-ue-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ue-management.component.html',
  styleUrls: ['./ue-management.component.css']
})
export class UEManagementComponent implements OnInit {

  // Data
  ues: UEDTO[] = [];
  filteredUEs: UEDTO[] = [];
  enseignants: any[] = [];
  statistics: UEStatistics | null = null;
  options: UEOptions | null = null;

  // UI State
  activeSection: string = 'list';
  isLoading: boolean = false;
  showAddForm: boolean = false;
  showEditForm: boolean = false;
  showAssignModal: boolean = false;

  // Forms
  newUE: UEDTO = this.initializeNewUE();
  editingUE: UEDTO | null = null;
  selectedUE: UEDTO | null = null;

  // Filters and Search
  searchTerm: string = '';
  selectedSemestre: string = '';
  selectedNiveau: string = '';
  sortBy: string = 'nomUE';
  sortDirection: string = 'asc';

  // Pagination
  currentPage: number = 0;
  pageSize: number = 10;
  totalElements: number = 0;
  totalPages: number = 0;

  constructor(
    private ueService: UEService,
    private userService: UserService
  ) { }

  ngOnInit(): void {
    this.loadInitialData();
  }

  loadInitialData(): void {
    this.loadUEs();
    this.loadEnseignants();
    this.loadStatistics();
    this.loadOptions();
  }

  // ========== Data Loading ==========

  loadUEs(): void {
    this.isLoading = true;
    this.ueService.getAllUEs().subscribe({
      next: (ues) => {
        this.ues = ues;
        this.applyFilters();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des UEs:', error);
        this.isLoading = false;
      }
    });
  }

  loadEnseignants(): void {
    this.userService.getEnseignants().subscribe({
      next: (enseignants) => {
        this.enseignants = enseignants;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des enseignants:', error);
      }
    });
  }

  loadStatistics(): void {
    this.ueService.getUEStatistics().subscribe({
      next: (stats) => {
        this.statistics = stats;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des statistiques:', error);
      }
    });
  }

  loadOptions(): void {
    this.ueService.getUEOptions().subscribe({
      next: (options) => {
        this.options = options;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des options:', error);
      }
    });
  }

  // ========== CRUD Operations ==========

  createUE(): void {
    if (!this.validateUE(this.newUE)) {
      return;
    }

    this.isLoading = true;
    this.ueService.createUE(this.newUE).subscribe({
      next: (createdUE) => {
        this.ues.push(createdUE);
        this.applyFilters();
        this.resetNewUE();
        this.showAddForm = false;
        this.isLoading = false;
        alert('UE créée avec succès !');
      },
      error: (error) => {
        console.error('Erreur lors de la création de l\'UE:', error);
        alert('Erreur lors de la création de l\'UE: ' + (error.error?.message || error.message));
        this.isLoading = false;
      }
    });
  }

  updateUE(): void {
    if (!this.editingUE || !this.validateUE(this.editingUE)) {
      return;
    }

    this.isLoading = true;
    this.ueService.updateUE(this.editingUE.id!, this.editingUE).subscribe({
      next: (updatedUE) => {
        const index = this.ues.findIndex(ue => ue.id === updatedUE.id);
        if (index !== -1) {
          this.ues[index] = updatedUE;
          this.applyFilters();
        }
        this.cancelEdit();
        this.isLoading = false;
        alert('UE mise à jour avec succès !');
      },
      error: (error) => {
        console.error('Erreur lors de la mise à jour de l\'UE:', error);
        alert('Erreur lors de la mise à jour de l\'UE: ' + (error.error?.message || error.message));
        this.isLoading = false;
      }
    });
  }

  deleteUE(ue: UEDTO): void {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer l'UE "${ue.nomUE}" ?`)) {
      return;
    }

    this.isLoading = true;
    this.ueService.deleteUE(ue.id!).subscribe({
      next: () => {
        this.ues = this.ues.filter(u => u.id !== ue.id);
        this.applyFilters();
        this.isLoading = false;
        alert('UE supprimée avec succès !');
      },
      error: (error) => {
        console.error('Erreur lors de la suppression de l\'UE:', error);
        alert('Erreur lors de la suppression de l\'UE: ' + (error.error?.message || error.message));
        this.isLoading = false;
      }
    });
  }

  // ========== UI Actions ==========

  setActiveSection(section: string): void {
    this.activeSection = section;
    if (section === 'statistics') {
      this.loadStatistics();
    }
  }

  showAddUEForm(): void {
    this.resetNewUE();
    this.showAddForm = true;
  }

  cancelAdd(): void {
    this.showAddForm = false;
    this.resetNewUE();
  }

  editUE(ue: UEDTO): void {
    this.editingUE = { ...ue };
    this.showEditForm = true;
  }

  cancelEdit(): void {
    this.showEditForm = false;
    this.editingUE = null;
  }

  showAssignEnseignantModal(ue: UEDTO): void {
    this.selectedUE = ue;
    this.showAssignModal = true;
  }

  closeAssignModal(): void {
    this.showAssignModal = false;
    this.selectedUE = null;
  }

  // ========== Enseignant Assignment ==========

  assignEnseignant(enseignantId: string): void {
    if (!this.selectedUE) return;

    this.ueService.assignEnseignantToUE(this.selectedUE.id!, enseignantId).subscribe({
      next: (updatedUE) => {
        const index = this.ues.findIndex(ue => ue.id === updatedUE.id);
        if (index !== -1) {
          this.ues[index] = updatedUE;
          this.applyFilters();
        }
        alert('Enseignant assigné avec succès !');
        this.closeAssignModal();
      },
      error: (error) => {
        console.error('Erreur lors de l\'assignation:', error);
        
        // Essayer la version simplifiée en cas d'erreur
        console.log('Tentative avec la version simplifiée...');
        this.ueService.assignEnseignantToUESimple(this.selectedUE!.id!, enseignantId).subscribe({
          next: (updatedUE) => {
            const index = this.ues.findIndex(ue => ue.id === updatedUE.id);
            if (index !== -1) {
              this.ues[index] = updatedUE;
              this.applyFilters();
            }
            alert('Enseignant assigné avec succès (version simplifiée) !');
            this.closeAssignModal();
          },
          error: (simpleError) => {
            console.error('Erreur lors de l\'assignation simplifiée:', simpleError);
            alert('Erreur lors de l\'assignation: ' + (simpleError.error?.message || simpleError.message));
          }
        });
      }
    });
  }

  removeEnseignant(ue: UEDTO, enseignantId: string): void {
    if (!confirm('Êtes-vous sûr de vouloir retirer cet enseignant de l\'UE ?')) {
      return;
    }

    this.ueService.removeEnseignantFromUE(ue.id!, enseignantId).subscribe({
      next: (updatedUE) => {
        const index = this.ues.findIndex(u => u.id === updatedUE.id);
        if (index !== -1) {
          this.ues[index] = updatedUE;
          this.applyFilters();
        }
        alert('Enseignant retiré avec succès !');
      },
      error: (error) => {
        console.error('Erreur lors du retrait:', error);
        alert('Erreur lors du retrait: ' + (error.error?.message || error.message));
      }
    });
  }

  // ========== Filters and Search ==========

  applyFilters(): void {
    let filtered = [...this.ues];

    // Search filter
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(ue => 
        ue.codeUE.toLowerCase().includes(term) ||
        ue.nomUE.toLowerCase().includes(term) ||
        (ue.description && ue.description.toLowerCase().includes(term))
      );
    }

    // Semestre filter
    if (this.selectedSemestre) {
      filtered = filtered.filter(ue => ue.semestre === this.selectedSemestre);
    }

    // Niveau filter
    if (this.selectedNiveau) {
      filtered = filtered.filter(ue => ue.niveau === this.selectedNiveau);
    }

    // Sort
    filtered.sort((a, b) => {
      let aValue: any = a[this.sortBy as keyof UEDTO];
      let bValue: any = b[this.sortBy as keyof UEDTO];

      if (typeof aValue === 'string') {
        aValue = aValue.toLowerCase();
        bValue = bValue.toLowerCase();
      }

      if (this.sortDirection === 'asc') {
        return aValue < bValue ? -1 : aValue > bValue ? 1 : 0;
      } else {
        return aValue > bValue ? -1 : aValue < bValue ? 1 : 0;
      }
    });

    this.filteredUEs = filtered;
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedSemestre = '';
    this.selectedNiveau = '';
    this.applyFilters();
  }

  // ========== Utility Methods ==========

  initializeNewUE(): UEDTO {
    return {
      codeUE: '',
      nomUE: '',
      description: '',
      heuresCM: 0,
      heuresTD: 0,
      heuresTP: 0,
      creditsECTS: 0,
      semestre: '',
      niveau: '',
      active: true
    };
  }

  resetNewUE(): void {
    this.newUE = this.initializeNewUE();
  }

  validateUE(ue: UEDTO): boolean {
    if (!ue.codeUE.trim()) {
      alert('Le code UE est obligatoire');
      return false;
    }
    if (!ue.nomUE.trim()) {
      alert('Le nom de l\'UE est obligatoire');
      return false;
    }
    if (ue.creditsECTS <= 0) {
      alert('Le nombre de crédits ECTS doit être positif');
      return false;
    }
    if ((ue.heuresCM + ue.heuresTD + ue.heuresTP) === 0) {
      alert('L\'UE doit avoir au moins une heure de cours');
      return false;
    }
    if (!ue.semestre.trim()) {
      alert('Le semestre est obligatoire');
      return false;
    }
    if (!ue.niveau.trim()) {
      alert('Le niveau est obligatoire');
      return false;
    }
    return true;
  }

  getTotalHeures(ue: UEDTO): number {
    return (ue.heuresCM || 0) + (ue.heuresTD || 0) + (ue.heuresTP || 0);
  }

  getTypeCoursDominant(ue: UEDTO): string {
    const cm = ue.heuresCM || 0;
    const td = ue.heuresTD || 0;
    const tp = ue.heuresTP || 0;

    if (cm >= td && cm >= tp) return 'CM';
    if (td >= tp) return 'TD';
    return 'TP';
  }

  getEnseignantName(enseignantId: string): string {
    const enseignant = this.enseignants.find(e => e.idUser === enseignantId);
    return enseignant ? `${enseignant.prenomUser} ${enseignant.nomUser}` : 'Inconnu';
  }

  isEnseignantAssigned(ue: UEDTO, enseignantId: string): boolean {
    return ue.enseignantIds ? ue.enseignantIds.includes(enseignantId) : false;
  }
}