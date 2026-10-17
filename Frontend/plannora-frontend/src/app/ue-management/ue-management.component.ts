import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UEService, UEDTO, UEStatistics } from '../services/ue.service';
import { ClassesService, FiliereDTO, SpecialiteDTO } from '../services/classes.service';
import { EcoleDTO, EcoleService } from '../services/ecole.service';
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
  filieres: FiliereDTO[] = [];
  specialites: SpecialiteDTO[] = [];
  ecoles: EcoleDTO[] = [];
  statistics: UEStatistics | null = null;

  activeSection: string = 'list';
  isLoading: boolean = false;
  showAddForm: boolean = false;
  showEditForm: boolean = false;

  newUE: UEDTO = this.emptyUE();
  editingUE: UEDTO | null = null;

  searchTerm: string = '';
  filterNiveau: string = '';
  filterFiliere: string = '';
  successMessage: string = '';
  errorMessage: string = '';

  readonly niveaux = ['ING1', 'ING2', 'ING3', 'ING4', 'ING5', 'L1', 'L2', 'L3', 'M1', 'M2', 'PREPA'];

  constructor(
    private ueService: UEService,
    private classesService: ClassesService,
    private notification: NotificationService,
    private ecoleService: EcoleService
  ) {}

  ngOnInit(): void {
    this.loadUEs();
    this.classesService.getFilieres().subscribe({
      next: (f) => this.filieres = f,
      error: () => this.errorMessage = 'Impossible de charger les départements.'
    });
    this.classesService.getSpecialites().subscribe({
      next: (specialites) => this.specialites = specialites,
      error: () => this.errorMessage = 'Impossible de charger les spécialités.'
    });
    this.ecoleService.obtenirToutes().subscribe({
      next: (ecoles) => this.ecoles = ecoles,
      error: () => this.errorMessage = 'Impossible de charger les écoles.'
    });
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
      error: (err) => {
        this.errorMessage = err.error?.message || 'Impossible de charger les UEs.';
        this.isLoading = false;
      }
    });
  }

  applyFilters(): void {
    const term = this.searchTerm.toLowerCase().trim();
    this.filteredUEs = this.ues.filter(ue => {
      if (term && !(ue.codeUE?.toLowerCase().includes(term) || ue.nomUE?.toLowerCase().includes(term) || ue.description?.toLowerCase().includes(term))) return false;
      if (this.filterNiveau && ue.niveau !== this.filterNiveau) return false;
      if (this.filterFiliere && ue.idFiliere !== this.filterFiliere) return false;
      return true;
    });
  }

  updateStatistics(): void {
    this.statistics = this.ueService.computeStatistics(this.ues);
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.filterNiveau = '';
    this.filterFiliere = '';
    this.applyFilters();
  }

  setActiveSection(section: string): void {
    this.activeSection = section;
    if (section === 'statistics') this.updateStatistics();
  }

  showAddUEForm(): void {
    this.newUE = this.emptyUE();
    this.showAddForm = true;
    this.clearMessages();
  }

  cancelAdd(): void {
    this.showAddForm = false;
    this.newUE = this.emptyUE();
  }

  editUE(ue: UEDTO): void {
    this.editingUE = { ...ue };
    this.showEditForm = true;
    this.clearMessages();
  }

  cancelEdit(): void {
    this.showEditForm = false;
    this.editingUE = null;
  }

  viewUE(ue: UEDTO): void {
    const lines = [
      `Code : ${ue.codeUE}`,
      `Nom : ${ue.nomUE}`,
      ue.description ? `Description : ${ue.description}` : '',
      ue.niveau ? `Niveau : ${ue.niveau}` : '',
      ue.nomFiliere ? `Département : ${ue.nomFiliere}` : '',
      ue.specialite ? `Spécialité : ${ue.specialite}` : '',
      ue.idEcole ? `École : ${this.ecoles.find(ecole => ecole.idEcole === ue.idEcole)?.nomEcole ?? ue.idEcole}` : '',
      ue.nombreCredit != null ? `Crédits : ${ue.nombreCredit}` : '',
      ue.heuresCM != null ? `CM : ${ue.heuresCM}h` : '',
      ue.heuresTD != null ? `TD : ${ue.heuresTD}h` : '',
      ue.heuresTP != null ? `TP : ${ue.heuresTP}h` : '',
    ].filter(Boolean);
    this.notification.info(lines.join('\n'), `Détails — ${ue.codeUE}`);
  }

  onFiliereChange(ue: UEDTO): void {
    const f = this.filieres.find(f => f.idFiliere === ue.idFiliere);
    ue.nomFiliere = f?.nom;
    ue.idSpecialite = '';
    ue.specialite = '';
  }

  onEcoleChange(ue: UEDTO): void {
    ue.idFiliere = '';
    ue.nomFiliere = '';
    ue.idSpecialite = '';
    ue.specialite = '';
  }

  onSpecialiteChange(ue: UEDTO): void {
    ue.specialite = this.specialites.find(item => item.idSpecialite === ue.idSpecialite)?.nom ?? '';
  }

  getEcoleName(idEcole: string): string {
    return this.ecoles.find(ecole => ecole.idEcole === idEcole)?.nomEcole ?? idEcole;
  }

  get departmentsForNewUE(): FiliereDTO[] {
    return this.filieres.filter(filiere => filiere.idEcole === this.newUE.idEcole);
  }

  get departmentsForEditingUE(): FiliereDTO[] {
    return this.editingUE
      ? this.filieres.filter(filiere => filiere.idEcole === this.editingUE!.idEcole)
      : [];
  }

  get specialitesForNewUE(): SpecialiteDTO[] {
    return this.specialites.filter(specialite => specialite.idFiliere === this.newUE.idFiliere);
  }

  get specialitesForEditingUE(): SpecialiteDTO[] {
    return this.editingUE
      ? this.specialites.filter(specialite => specialite.idFiliere === this.editingUE!.idFiliere)
      : [];
  }

  createUE(): void {
    if (!this.validate(this.newUE)) return;
    this.isLoading = true;
    this.clearMessages();
    this.ueService.createUE(this.newUE).subscribe({
      next: (created) => {
        this.ues.push(created);
        this.applyFilters();
        this.updateStatistics();
        this.showAddForm = false;
        this.newUE = this.emptyUE();
        this.isLoading = false;
        this.successMessage = 'UE créée avec succès.';
        setTimeout(() => { this.successMessage = ''; }, 3000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors de la création.';
        this.isLoading = false;
      }
    });
  }

  updateUE(): void {
    if (!this.editingUE?.id || !this.validate(this.editingUE)) return;
    this.isLoading = true;
    this.clearMessages();
    this.ueService.updateUE(this.editingUE.id, this.editingUE).subscribe({
      next: (updated) => {
        const i = this.ues.findIndex(u => u.id === updated.id);
        if (i !== -1) this.ues[i] = updated;
        this.applyFilters();
        this.updateStatistics();
        this.cancelEdit();
        this.isLoading = false;
        this.successMessage = 'UE mise à jour avec succès.';
        setTimeout(() => { this.successMessage = ''; }, 3000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erreur lors de la mise à jour.';
        this.isLoading = false;
      }
    });
  }

  deleteUE(ue: UEDTO): void {
    if (!ue.id) return;
    this.notification.confirm(
      `Supprimer l'UE "${ue.nomUE}" (${ue.codeUE}) ?`,
      'Confirmation',
      () => {
        this.isLoading = true;
        this.ueService.deleteUE(ue.id!).subscribe({
          next: () => {
            this.ues = this.ues.filter(u => u.id !== ue.id);
            this.applyFilters();
            this.updateStatistics();
            this.isLoading = false;
            this.successMessage = 'UE supprimée avec succès.';
            setTimeout(() => { this.successMessage = ''; }, 3000);
          },
          error: (err) => {
            this.errorMessage = err.error?.message || 'Erreur lors de la suppression.';
            this.isLoading = false;
          }
        });
      }
    );
  }

  private validate(ue: UEDTO): boolean {
    if (!ue.codeUE?.trim()) { this.errorMessage = 'Le code UE est obligatoire.'; return false; }
    if (!ue.nomUE?.trim()) { this.errorMessage = "Le nom de l'UE est obligatoire."; return false; }
    if (!ue.idEcole || !ue.idFiliere || !ue.idSpecialite || !ue.specialite) {
      this.errorMessage = "L'école, le département et la spécialité sont obligatoires.";
      return false;
    }
    return true;
  }

  private emptyUE(): UEDTO {
    return {
      codeUE: '', nomUE: '', description: '',
      nombreCredit: undefined, nombreHeur: undefined,
      heuresCM: undefined, heuresTD: undefined, heuresTP: undefined,
      heureDebut: '08:00', heureFin: '18:00',
      niveau: undefined, idFiliere: '', nomFiliere: '',
      idEcole: '', idSpecialite: '', specialite: ''
    };
  }

  clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }
}
