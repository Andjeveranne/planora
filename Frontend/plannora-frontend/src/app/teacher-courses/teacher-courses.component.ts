import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmploiDuTempsService, UEEnseignantDTO } from '../services/emploi-du-temps.service';
import { UEService, UEDTO } from '../services/ue.service';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';

@Component({
  selector: 'app-teacher-courses',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './teacher-courses.component.html',
  styleUrls: ['./teacher-courses.component.css']
})
export class TeacherCoursesComponent implements OnInit {
  mesUEEnseignants: UEEnseignantDTO[] = [];
  toutesLesUEs: UEDTO[] = [];
  loading = false;
  savingId: number | null = null;

  currentTeacherId = '';

  // Formulaire d'ajout
  showAddForm = false;
  selectedUEId: number | null = null;
  newPeutCM = false;
  newPeutTD = false;
  newPeutTP = false;
  adding = false;

  constructor(
    private emploiDuTempsService: EmploiDuTempsService,
    private ueService: UEService,
    private authService: AuthService,
    private notification: NotificationService
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    if (!user) {
      this.notification.error('Vous devez être connecté pour accéder à cette page.');
      return;
    }
    this.currentTeacherId = String(user.userId);
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    // Load teacher's UE assignments and all available UEs in parallel
    let ueDone = false;
    let assignDone = false;

    this.ueService.getAllUEs().subscribe({
      next: (ues) => {
        this.toutesLesUEs = ues;
        ueDone = true;
        if (assignDone) this.loading = false;
      },
      error: () => {
        this.toutesLesUEs = [];
        ueDone = true;
        if (assignDone) this.loading = false;
      }
    });

    this.emploiDuTempsService.getUEEnseignantsParEnseignant(this.currentTeacherId).subscribe({
      next: (data) => {
        this.mesUEEnseignants = data;
        assignDone = true;
        if (ueDone) this.loading = false;
      },
      error: () => {
        this.mesUEEnseignants = [];
        assignDone = true;
        if (ueDone) this.loading = false;
      }
    });
  }

  /** UEs pas encore assignées à cet enseignant */
  get uesDisponibles(): UEDTO[] {
    const assignedIds = new Set(this.mesUEEnseignants.map(ue => ue.ueId));
    return this.toutesLesUEs.filter(ue => ue.id != null && !assignedIds.has(ue.id));
  }

  getNomUE(ueId: number): string {
    const ue = this.toutesLesUEs.find(u => u.id === ueId);
    return ue ? `${ue.codeUE} – ${ue.nomUE}` : `UE #${ueId}`;
  }

  toggleAddForm(): void {
    this.showAddForm = !this.showAddForm;
    if (!this.showAddForm) this.resetAddForm();
  }

  resetAddForm(): void {
    this.selectedUEId = null;
    this.newPeutCM = false;
    this.newPeutTD = false;
    this.newPeutTP = false;
  }

  ajouterUE(): void {
    if (!this.selectedUEId) {
      this.notification.warning('Veuillez sélectionner une UE.');
      return;
    }
    if (!this.newPeutCM && !this.newPeutTD && !this.newPeutTP) {
      this.notification.warning('Veuillez sélectionner au moins un type de cours (CM, TD ou TP).');
      return;
    }

    this.adding = true;
    const dto: UEEnseignantDTO = {
      ueId: this.selectedUEId,
      enseignantId: this.currentTeacherId,
      peutCM: this.newPeutCM,
      peutTD: this.newPeutTD,
      peutTP: this.newPeutTP
    };

    this.emploiDuTempsService.creerUEEnseignant(dto).subscribe({
      next: (created) => {
        // Enrich with UE info from local cache
        const ueInfo = this.toutesLesUEs.find(u => u.id === created.ueId);
        if (ueInfo && !created.codeUE) {
          created.codeUE = ueInfo.codeUE;
          created.nomUE = ueInfo.nomUE;
        }
        this.mesUEEnseignants = [...this.mesUEEnseignants, created];
        this.notification.success('UE ajoutée avec succès.');
        this.showAddForm = false;
        this.resetAddForm();
        this.adding = false;
      },
      error: (err) => {
        const msg = err?.error?.message ?? 'Erreur lors de l\'ajout de l\'UE.';
        this.notification.error(msg);
        this.adding = false;
      }
    });
  }

  updateTypes(entry: UEEnseignantDTO): void {
    if (!entry.id) return;
    this.savingId = entry.id;

    this.emploiDuTempsService.mettreAJourUEEnseignant(entry.id, entry).subscribe({
      next: (updated) => {
        const idx = this.mesUEEnseignants.findIndex(e => e.id === updated.id);
        if (idx !== -1) {
          // preserve enriched fields if backend doesn't return them
          this.mesUEEnseignants[idx] = {
            ...this.mesUEEnseignants[idx],
            peutCM: updated.peutCM,
            peutTD: updated.peutTD,
            peutTP: updated.peutTP
          };
        }
        this.savingId = null;
      },
      error: () => {
        this.notification.error('Erreur lors de la mise à jour.');
        this.savingId = null;
        // reload to restore original state
        this.loadData();
      }
    });
  }

  supprimerUE(entry: UEEnseignantDTO): void {
    const nom = entry.nomUE ?? this.getNomUE(entry.ueId);
    this.notification.confirm(
      `Retirer l'UE "${nom}" de votre liste ?`,
      'Confirmation',
      () => {
        this.emploiDuTempsService.supprimerUEEnseignant(entry.id!).subscribe({
          next: () => {
            this.mesUEEnseignants = this.mesUEEnseignants.filter(e => e.id !== entry.id);
            this.notification.success('UE retirée.');
          },
          error: () => this.notification.error('Erreur lors de la suppression.')
        });
      }
    );
  }

  get totalCM(): number {
    return this.mesUEEnseignants.filter(e => e.peutCM).length;
  }
  get totalTD(): number {
    return this.mesUEEnseignants.filter(e => e.peutTD).length;
  }
  get totalTP(): number {
    return this.mesUEEnseignants.filter(e => e.peutTP).length;
  }
}
