import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UEService, UEDTO, UEStatistics } from '../services/ue.service';
import { NotificationService } from '../services/notification.service';

@Component({
  selector: 'app-ue-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ue-management.component.html',
  styleUrls: ['./ue-management.component.css']
})
export class UEManagementComponent implements OnInit {

  ues: UEDTO[] = [];
  filteredUEs: UEDTO[] = [];
  statistics: UEStatistics | null = null;

  activeSection: string = 'list';
  isLoading: boolean = false;
  showAddForm: boolean = false;
  showEditForm: boolean = false;

  newUE: UEDTO = this.initializeNewUE();
  editingUE: UEDTO | null = null;

  searchTerm: string = '';
  sortBy: string = 'nomUE';
  sortDirection: string = 'asc';

  successMessage: string = '';
  errorMessage: string = '';

  constructor(
    private ueService: UEService,
    private notification: NotificationService
  ) { }

  ngOnInit(): void {
    this.loadUEs();
  }

  loadUEs(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.ueService.getAllUEs().subscribe({
      next: (ues) => {
        this.ues = ues;
        this.applyFilters();
        this.updateStatistics();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des UEs:', error);
        this.errorMessage = error.error?.message || error.message || 'Impossible de charger les UEs.';
        this.isLoading = false;
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.ues];

    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(ue =>
        ue.codeUE?.toLowerCase().includes(term) ||
        ue.nomUE?.toLowerCase().includes(term) ||
        (ue.description && ue.description.toLowerCase().includes(term))
      );
    }

    filtered.sort((a, b) => {
      let aVal: any = a[this.sortBy as keyof UEDTO];
      let bVal: any = b[this.sortBy as keyof UEDTO];
      if (typeof aVal === 'string') {
        aVal = aVal?.toLowerCase() ?? '';
        bVal = bVal?.toLowerCase() ?? '';
      }
      if (aVal == null) aVal = '';
      if (bVal == null) bVal = '';
      if (this.sortDirection === 'asc') {
        return aVal < bVal ? -1 : aVal > bVal ? 1 : 0;
      }
      return aVal > bVal ? -1 : aVal < bVal ? 1 : 0;
    });

    this.filteredUEs = filtered;
  }

  updateStatistics(): void {
    this.statistics = this.ueService.computeStatistics(this.ues);
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.applyFilters();
  }

  setActiveSection(section: string): void {
    this.activeSection = section;
    if (section === 'statistics') {
      this.updateStatistics();
    }
  }

  showAddUEForm(): void {
    this.resetNewUE();
    this.showAddForm = true;
    this.clearMessages();
  }

  cancelAdd(): void {
    this.showAddForm = false;
    this.resetNewUE();
  }

  editUE(ue: UEDTO): void {
    this.editingUE = { ...ue };
    this.showEditForm = true;
    this.clearMessages();
  }

  viewUE(ue: UEDTO): void {
    const q = ue.quantite != null ? `Quantité: ${ue.quantite}` : '';
    const c = ue.nCredit != null ? `Crédits: ${ue.nCredit}` : '';
    const parts = [ue.codeUE, ue.nomUE, ue.description, q, c].filter(Boolean);
    this.notification.info(parts.join('\n'), `Détails - ${ue.codeUE}`);
  }

  cancelEdit(): void {
    this.showEditForm = false;
    this.editingUE = null;
  }

  createUE(): void {
    if (!this.validateUE(this.newUE)) return;

    this.isLoading = true;
    this.clearMessages();
    this.ueService.createUE(this.newUE).subscribe({
      next: (created) => {
        this.ues.push(created);
        this.applyFilters();
        this.updateStatistics();
        this.resetNewUE();
        this.showAddForm = false;
        this.isLoading = false;
        this.successMessage = 'UE créée avec succès.';
      },
      error: (error) => {
        this.errorMessage = error.error?.message || error.message || 'Erreur lors de la création.';
        this.isLoading = false;
      }
    });
  }

  updateUE(): void {
    if (!this.editingUE || !this.editingUE.idUE || !this.validateUE(this.editingUE)) return;

    this.isLoading = true;
    this.clearMessages();
    this.ueService.updateUE(this.editingUE.idUE, this.editingUE).subscribe({
      next: (updated) => {
        const index = this.ues.findIndex(ue => ue.idUE === updated.idUE);
        if (index !== -1) this.ues[index] = updated;
        this.applyFilters();
        this.updateStatistics();
        this.cancelEdit();
        this.isLoading = false;
        this.successMessage = 'UE mise à jour avec succès.';
      },
      error: (error) => {
        this.errorMessage = error.error?.message || error.message || 'Erreur lors de la mise à jour.';
        this.isLoading = false;
      }
    });
  }

  deleteUE(ue: UEDTO): void {
    if (!ue.idUE) return;
    this.notification.confirm(
      `Supprimer l'UE "${ue.nomUE}" ?`,
      'Confirmation',
      () => this.doDeleteUE(ue)
    );
  }

  private doDeleteUE(ue: UEDTO): void {
    if (!ue.idUE) return;
    this.isLoading = true;
    this.clearMessages();
    this.ueService.deleteUE(ue.idUE).subscribe({
      next: () => {
        this.ues = this.ues.filter(u => u.idUE !== ue.idUE);
        this.applyFilters();
        this.updateStatistics();
        this.isLoading = false;
        this.successMessage = 'UE supprimée avec succès.';
      },
      error: (error) => {
        this.errorMessage = error.error?.message || error.message || 'Erreur lors de la suppression.';
        this.isLoading = false;
      }
    });
  }

  initializeNewUE(): UEDTO {
    return {
      codeUE: '',
      nomUE: '',
      description: '',
      quantite: undefined,
      nCredit: undefined
    };
  }

  resetNewUE(): void {
    this.newUE = this.initializeNewUE();
  }

  validateUE(ue: UEDTO): boolean {
    if (!ue.codeUE?.trim()) {
      this.errorMessage = 'Le code UE est obligatoire.';
      return false;
    }
    if (!ue.nomUE?.trim()) {
      this.errorMessage = 'Le nom de l\'UE est obligatoire.';
      return false;
    }
    return true;
  }

  clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }

  getQuantite(ue: UEDTO): number {
    return ue.quantite ?? 0;
  }
}
