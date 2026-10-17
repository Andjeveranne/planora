import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClassesService, ClasseDTO, FiliereDTO, SpecialiteDTO } from '../../services/classes.service';
import { EcoleDTO, EcoleService } from '../../services/ecole.service';
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
  specialites: SpecialiteDTO[] = [];
  ecoles: EcoleDTO[] = [];

  // UI state
  isLoading = false;
  activeTab: 'classes' | 'departements' | 'specialites' = 'classes';
  errorMessage = '';
  successMessage = '';

  // Class filters
  filtreRecherche = '';
  filtreDepartement = '';
  filtreNiveau = '';

  // Modal classe
  showClasseModal = false;
  isEditClasse = false;
  editingClasseId: string | null = null;
  classeForm: ClasseDTO = this.emptyClasse();
  selectedEcoleId = '';

  // Department modal (the API still uses the legacy Filiere DTO)
  showFiliereModal = false;
  isEditFiliere = false;
  editingFiliereId: string | null = null;
  filiereForm: FiliereDTO = this.emptyFiliere();

  showSpecialiteModal = false;
  isEditSpecialite = false;
  editingSpecialiteId: string | null = null;
  specialiteForm: SpecialiteDTO = this.emptySpecialite();
  selectedSpecialiteEcoleId = '';

  readonly niveaux = ['L1', 'L2', 'L3', 'L4', 'L5', 'Master1', 'Master2'];

  constructor(
    private classesService: ClassesService,
    private ecoleService: EcoleService,
    private notification: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadAll();
    this.ecoleService.obtenirToutes().subscribe({
      next: (ecoles) => this.ecoles = ecoles,
      error: () => this.errorMessage = 'Erreur chargement écoles'
    });
  }

  loadAll(): void {
    this.isLoading = true;
    this.classesService.getFilieres().subscribe({
      next: (f) => {
        this.filieres = f;
        this.classesService.getSpecialites().subscribe({
          next: (specialites) => {
            this.specialites = specialites;
            this.classesService.getClasses().subscribe({
              next: (c) => { this.classes = c; this.isLoading = false; },
              error: () => { this.errorMessage = 'Erreur chargement classes'; this.isLoading = false; }
            });
          },
          error: () => { this.errorMessage = 'Erreur chargement spécialités'; this.isLoading = false; }
        });
      },
      error: () => { this.errorMessage = 'Erreur chargement des départements'; this.isLoading = false; }
    });
  }

  get filteredClasses(): ClasseDTO[] {
    const term = this.filtreRecherche.toLowerCase();
    return this.classes.filter(c =>
      (!term || c.nomClasse.toLowerCase().includes(term) || (c.nomFiliere || '').toLowerCase().includes(term)) &&
      (!this.filtreDepartement || c.idFiliere === this.filtreDepartement) &&
      (!this.filtreNiveau || c.niveau === this.filtreNiveau)
    );
  }

  // ===== CLASSES =====
  openAddClasse(): void {
    this.classeForm = this.emptyClasse();
    this.selectedEcoleId = '';
    this.isEditClasse = false;
    this.editingClasseId = null;
    this.showClasseModal = true;
    this.clearMessages();
  }

  openEditClasse(c: ClasseDTO): void {
    this.classeForm = { ...c };
    const filiere = this.filieres.find(item => item.idFiliere === c.idFiliere);
    this.selectedEcoleId = c.idEcole || filiere?.idEcole || '';
    this.isEditClasse = true;
    this.editingClasseId = c.idClasse!;
    this.showClasseModal = true;
    this.clearMessages();
  }

  saveClasse(): void {
    if (!this.classeForm.nomClasse || !this.classeForm.niveau || !this.selectedEcoleId ||
        !this.classeForm.idFiliere || !this.classeForm.specialite || !this.classeForm.effectifs) {
      this.errorMessage = 'Le nom, le niveau, l’école, le département, la spécialité et l’effectif sont obligatoires.';
      return;
    }
    this.isLoading = true;
    this.classeForm.idEcole = this.selectedEcoleId;
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

  // ===== DEPARTMENTS (stored as filieres for backend compatibility) =====
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
    if (!this.filiereForm.nom || !this.filiereForm.idEcole) {
      this.errorMessage = 'Le nom et l’école sont obligatoires.';
      return;
    }
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
        this.successMessage = `Département ${this.isEditFiliere ? 'modifié' : 'créé'} avec succès.`;
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
    this.notification.confirm(`Supprimer le département "${f.nom}" et toutes ses classes ?`, 'Confirmation', () => {
      this.classesService.deleteFiliere(f.idFiliere!).subscribe({
        next: () => {
          this.filieres = this.filieres.filter(x => x.idFiliere !== f.idFiliere);
          this.classes = this.classes.filter(c => c.idFiliere !== f.idFiliere);
          this.successMessage = 'Département supprimé.';
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => { this.errorMessage = err.error?.message || 'Erreur suppression.'; }
      });
    });
  }

  getClassesCount(idFiliere: string): number {
    return this.classes.filter(c => c.idFiliere === idFiliere).length;
  }

  getEcoleName(idEcole?: string): string {
    return this.ecoles.find(ecole => ecole.idEcole === idEcole)?.nomEcole ?? 'Non associée';
  }

  get departmentsForSelectedSchool(): FiliereDTO[] {
    return this.filieres.filter(filiere => filiere.idEcole === this.selectedEcoleId);
  }

  get specialtiesForSelectedDepartment(): string[] {
    return this.specialites
      .filter(specialite => specialite.idFiliere === this.classeForm.idFiliere)
      .map(specialite => specialite.nom)
      .sort((a, b) => a.localeCompare(b));
  }

  onClasseSchoolChange(): void {
    this.classeForm.idFiliere = '';
    this.classeForm.specialite = '';
  }

  onClasseDepartmentChange(): void {
    this.classeForm.specialite = '';
  }

  get specialitesByDepartment(): SpecialiteDTO[] {
    return this.specialites.filter(specialite =>
      !this.filtreDepartement || specialite.idFiliere === this.filtreDepartement
    );
  }

  openAddSpecialite(): void {
    this.specialiteForm = this.emptySpecialite();
    this.selectedSpecialiteEcoleId = '';
    this.isEditSpecialite = false;
    this.editingSpecialiteId = null;
    this.showSpecialiteModal = true;
    this.clearMessages();
  }

  openEditSpecialite(specialite: SpecialiteDTO): void {
    this.specialiteForm = { ...specialite };
    this.selectedSpecialiteEcoleId = specialite.idEcole ?? '';
    this.isEditSpecialite = true;
    this.editingSpecialiteId = specialite.idSpecialite!;
    this.showSpecialiteModal = true;
    this.clearMessages();
  }

  saveSpecialite(): void {
    if (!this.specialiteForm.nom.trim() || !this.specialiteForm.idFiliere) {
      this.errorMessage = 'Le nom et le département de la spécialité sont obligatoires.';
      return;
    }
    this.isLoading = true;
    const operation = this.isEditSpecialite
      ? this.classesService.updateSpecialite(this.editingSpecialiteId!, this.specialiteForm)
      : this.classesService.createSpecialite(this.specialiteForm);

    operation.subscribe({
      next: (saved) => {
        if (this.isEditSpecialite) {
          const index = this.specialites.findIndex(item => item.idSpecialite === this.editingSpecialiteId);
          if (index !== -1) this.specialites[index] = saved;
        } else {
          this.specialites.push(saved);
        }
        this.successMessage = `Spécialité ${this.isEditSpecialite ? 'modifiée' : 'créée'} avec succès.`;
        this.showSpecialiteModal = false;
        this.isLoading = false;
      },
      error: (err) => {
        const responseError = err.error;
        const serverMessage = typeof responseError === 'string'
          ? responseError
          : responseError?.detail || responseError?.message || responseError?.error;
        this.errorMessage = serverMessage ||
          `Erreur lors de l’enregistrement de la spécialité (HTTP ${err.status}).`;
        this.isLoading = false;
      }
    });
  }

  deleteSpecialite(specialite: SpecialiteDTO): void {
    this.notification.confirm(`Supprimer la spécialité "${specialite.nom}" ?`, 'Confirmation', () => {
      this.classesService.deleteSpecialite(specialite.idSpecialite!).subscribe({
        next: () => {
          this.specialites = this.specialites.filter(item => item.idSpecialite !== specialite.idSpecialite);
          this.successMessage = 'Spécialité supprimée.';
        },
        error: (err) => { this.errorMessage = err.error?.message || 'Erreur suppression.'; }
      });
    });
  }

  getDepartmentName(idFiliere: string): string {
    return this.filieres.find(filiere => filiere.idFiliere === idFiliere)?.nom ?? 'Département inconnu';
  }

  get departmentsForSpecialiteSchool(): FiliereDTO[] {
    return this.filieres.filter(filiere => filiere.idEcole === this.selectedSpecialiteEcoleId);
  }

  onSpecialiteSchoolChange(): void {
    if (!this.departmentsForSpecialiteSchool.some(filiere => filiere.idFiliere === this.specialiteForm.idFiliere)) {
      this.specialiteForm.idFiliere = '';
    }
  }

  clearMessages(): void { this.errorMessage = ''; this.successMessage = ''; }

  private emptyClasse(): ClasseDTO {
    return { nomClasse: '', effectifs: 30, niveau: '', idFiliere: '', specialite: '' };
  }

  private emptyFiliere(): FiliereDTO {
    return { nom: '', description: '', idEcole: '' };
  }

  private emptySpecialite(): SpecialiteDTO {
    return { nom: '', description: '', idFiliere: '' };
  }
}
