import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClassesService, ClasseDTO, FiliereDTO } from '../../services/classes.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-gestion-classes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gestion-classes.component.html',
  styleUrls: ['./gestion-classes.component.css']
})
export class GestionClassesComponent implements OnInit {

  // Data
  classes: ClasseDTO[] = [];
  filieres: FiliereDTO[] = [];

  // UI state
  isLoading = false;
  activeTab: 'classes' | 'filieres' = 'classes';
  errorMessage = '';
  successMessage = '';

  // Filtres classes
  filtreRecherche = '';
  filtreFiliere = '';
  filtreNiveau = '';

  // Modal classe
  showClasseModal = false;
  isEditClasse = false;
  editingClasseId: string | null = null;
  classeForm: ClasseDTO = this.emptyClasse();

  // Modal filière
  showFiliereModal = false;
  isEditFiliere = false;
  editingFiliereId: string | null = null;
  filiereForm: FiliereDTO = this.emptyFiliere();

  readonly niveaux = ['ING1', 'ING2', 'ING3', 'ING4', 'ING5'];

  constructor(
    private classesService: ClassesService,
    private notification: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.isLoading = true;
    this.classesService.getFilieres().subscribe({
      next: (f) => {
        this.filieres = f;
        this.classesService.getClasses().subscribe({
          next: (c) => { this.classes = c; this.isLoading = false; },
          error: () => { this.errorMessage = 'Erreur chargement classes'; this.isLoading = false; }
        });
      },
      error: () => { this.errorMessage = 'Erreur chargement filières'; this.isLoading = false; }
    });
  }

  get filteredClasses(): ClasseDTO[] {
    const term = this.filtreRecherche.toLowerCase();
    return this.classes.filter(c =>
      (!term || c.nomClasse.toLowerCase().includes(term) || (c.nomFiliere || '').toLowerCase().includes(term)) &&
      (!this.filtreFiliere || c.idFiliere === this.filtreFiliere) &&
      (!this.filtreNiveau || c.niveau === this.filtreNiveau)
    );
  }

  // ===== CLASSES =====
  openAddClasse(): void {
    this.classeForm = this.emptyClasse();
    this.isEditClasse = false;
    this.editingClasseId = null;
    this.showClasseModal = true;
    this.clearMessages();
  }

  openEditClasse(c: ClasseDTO): void {
    this.classeForm = { ...c };
    this.isEditClasse = true;
    this.editingClasseId = c.idClasse!;
    this.showClasseModal = true;
    this.clearMessages();
  }

  saveClasse(): void {
    if (!this.classeForm.nomClasse || !this.classeForm.niveau || !this.classeForm.idFiliere) {
      this.errorMessage = 'Nom, niveau et filière sont obligatoires.';
      return;
    }
    this.isLoading = true;
    const op = this.isEditClasse
      ? this.classesService.updateClasse(this.editingClasseId!, this.classeForm)
      : this.classesService.createClasse(this.classeForm);

    op.subscribe({
      next: (saved) => {
        if (this.isEditClasse) {
          const i = this.classes.findIndex(c => c.idClasse === this.editingClasseId);
          if (i !== -1) this.classes[i] = saved;
        } else {
          this.classes.push(saved);
        }
        this.successMessage = `Classe ${this.isEditClasse ? 'modifiée' : 'créée'} avec succès.`;
        this.showClasseModal = false;
        this.isLoading = false;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors de l\'opération.';
        this.isLoading = false;
      }
    });
  }

  deleteClasse(c: ClasseDTO): void {
    this.notification.confirm(`Supprimer la classe "${c.nomClasse}" ?`, 'Confirmation', () => {
      this.classesService.deleteClasse(c.idClasse!).subscribe({
        next: () => {
          this.classes = this.classes.filter(x => x.idClasse !== c.idClasse);
          this.successMessage = 'Classe supprimée.';
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => { this.errorMessage = err.error?.message || 'Erreur suppression.'; }
      });
    });
  }

  // ===== FILIERES =====
  openAddFiliere(): void {
    this.filiereForm = this.emptyFiliere();
    this.isEditFiliere = false;
    this.editingFiliereId = null;
    this.showFiliereModal = true;
    this.clearMessages();
  }

  openEditFiliere(f: FiliereDTO): void {
    this.filiereForm = { ...f };
    this.isEditFiliere = true;
    this.editingFiliereId = f.idFiliere!;
    this.showFiliereModal = true;
    this.clearMessages();
  }

  saveFiliere(): void {
    if (!this.filiereForm.nom) { this.errorMessage = 'Le nom est obligatoire.'; return; }
    this.isLoading = true;
    const op = this.isEditFiliere
      ? this.classesService.updateFiliere(this.editingFiliereId!, this.filiereForm)
      : this.classesService.createFiliere(this.filiereForm);

    op.subscribe({
      next: (saved) => {
        if (this.isEditFiliere) {
          const i = this.filieres.findIndex(f => f.idFiliere === this.editingFiliereId);
          if (i !== -1) this.filieres[i] = saved;
        } else {
          this.filieres.push(saved);
        }
        this.successMessage = `Filière ${this.isEditFiliere ? 'modifiée' : 'créée'} avec succès.`;
        this.showFiliereModal = false;
        this.isLoading = false;
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors de l\'opération.';
        this.isLoading = false;
      }
    });
  }

  deleteFiliere(f: FiliereDTO): void {
    this.notification.confirm(`Supprimer la filière "${f.nom}" et toutes ses classes ?`, 'Confirmation', () => {
      this.classesService.deleteFiliere(f.idFiliere!).subscribe({
        next: () => {
          this.filieres = this.filieres.filter(x => x.idFiliere !== f.idFiliere);
          this.classes = this.classes.filter(c => c.idFiliere !== f.idFiliere);
          this.successMessage = 'Filière supprimée.';
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => { this.errorMessage = err.error?.message || 'Erreur suppression.'; }
      });
    });
  }

  getClassesCount(idFiliere: string): number {
    return this.classes.filter(c => c.idFiliere === idFiliere).length;
  }

  clearMessages(): void { this.errorMessage = ''; this.successMessage = ''; }

  private emptyClasse(): ClasseDTO {
    return { nomClasse: '', effectifs: 30, niveau: '', idFiliere: '' };
  }

  private emptyFiliere(): FiliereDTO {
    return { nom: '', description: '' };
  }
}
