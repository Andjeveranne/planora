import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { FormsModule } from '@angular/forms';
import { EcoleService, EcoleDTO } from '../../services/ecole.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-gestion-ecoles',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './gestion-ecoles.component.html',
  styleUrls: ['./gestion-ecoles.component.css']
})
export class GestionEcolesComponent implements OnInit {
  ecoles: EcoleDTO[] = [];
  ecoleForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  showModal = false;
  isEditMode = false;
  currentEcoleId = '';

  filtreRecherche = '';

  currentPage = 1;
  pageSize = 10;
  totalItems = 0;
  totalPages = 0;

  sortField = '';
  sortDirection: 'asc' | 'desc' = 'asc';

  constructor(
    private ecoleService: EcoleService,
    private fb: FormBuilder,
    private notification: NotificationService
  ) {
    this.ecoleForm = this.fb.group({
      idEcole: [''], // optionnel en création (généré côté backend si vide)
      nomEcole: ['', [Validators.required, Validators.minLength(2)]]
    });
  }

  ngOnInit(): void {
    this.chargerEcoles();
  }

  chargerEcoles(): void {
    this.isLoading = true;
    this.ecoleService.obtenirToutes().subscribe({
      next: (ecoles) => {
        this.ecoles = ecoles;
        this.updatePagination();
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Erreur lors du chargement des écoles';
        this.isLoading = false;
        console.error('Erreur:', error);
      }
    });
  }

  get ecolesFiltrees(): EcoleDTO[] {
    let filtered = this.ecoles.filter(ecole => {
      const term = (this.filtreRecherche || '').toLowerCase();
      if (!term) return true;
      return (ecole.nomEcole || '').toLowerCase().includes(term) ||
             (ecole.idEcole || '').toLowerCase().includes(term);
    });

    if (this.sortField) {
      filtered = [...filtered].sort((a, b) => {
        const aVal = (a as any)[this.sortField] ?? '';
        const bVal = (b as any)[this.sortField] ?? '';
        const cmp = aVal < bVal ? -1 : aVal > bVal ? 1 : 0;
        return this.sortDirection === 'asc' ? cmp : -cmp;
      });
    }

    this.totalItems = filtered.length;
    this.totalPages = Math.max(1, Math.ceil(this.totalItems / this.pageSize));
    const start = (this.currentPage - 1) * this.pageSize;
    return filtered.slice(start, start + this.pageSize);
  }

  sortBy(field: string): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
  }

  updatePagination(): void {
    this.totalItems = this.ecoles.length;
    this.totalPages = Math.max(1, Math.ceil(this.totalItems / this.pageSize));
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) this.currentPage = page;
  }

  getVisiblePages(): number[] {
    const start = Math.max(1, this.currentPage - 2);
    const end = Math.min(this.totalPages, this.currentPage + 2);
    const pages: number[] = [];
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  }

  ouvrirModal(ecole?: EcoleDTO): void {
    this.showModal = true;
    this.isEditMode = !!ecole;
    this.errorMessage = '';
    this.successMessage = '';

    if (ecole) {
      this.currentEcoleId = ecole.idEcole!;
      this.ecoleForm.patchValue({ idEcole: ecole.idEcole, nomEcole: ecole.nomEcole });
    } else {
      this.ecoleForm.reset({ idEcole: '', nomEcole: '' });
      this.currentEcoleId = '';
    }
  }

  fermerModal(): void {
    this.showModal = false;
    this.ecoleForm.reset({ idEcole: '', nomEcole: '' });
    this.currentEcoleId = '';
  }

  onSubmit(): void {
    if (this.ecoleForm.invalid) {
      this.ecoleForm.markAllAsTouched();
      return;
    }
    this.isLoading = true;
    const dto: EcoleDTO = {
      idEcole: this.ecoleForm.get('idEcole')?.value || undefined,
      nomEcole: this.ecoleForm.get('nomEcole')?.value?.trim()
    };

    const op = this.isEditMode
      ? this.ecoleService.mettreAJour(this.currentEcoleId, dto)
      : this.ecoleService.creer(dto);

    op.subscribe({
      next: () => {
        this.successMessage = this.isEditMode ? 'École mise à jour avec succès' : 'École créée avec succès';
        this.chargerEcoles();
        this.fermerModal();
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Erreur lors de l\'opération';
        this.isLoading = false;
        console.error('Erreur:', error);
      }
    });
  }

  supprimerEcole(ecole: EcoleDTO): void {
    this.notification.confirm(
      `Supprimer l'école « ${ecole.nomEcole} » ? Cette action est irréversible.`,
      'Confirmation de suppression',
      () => {
        this.ecoleService.supprimer(ecole.idEcole!).subscribe({
          next: () => {
            this.successMessage = 'École supprimée avec succès';
            this.chargerEcoles();
          },
          error: (error) => {
            this.errorMessage = error.error?.message || 'Erreur lors de la suppression';
            console.error('Erreur:', error);
          }
        });
      }
    );
  }

  clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  get nomEcole() { return this.ecoleForm.get('nomEcole'); }
  get idEcole() { return this.ecoleForm.get('idEcole'); }
}
