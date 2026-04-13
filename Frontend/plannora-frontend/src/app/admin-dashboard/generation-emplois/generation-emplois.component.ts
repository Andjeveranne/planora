import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EmploiDuTempsService, EmploiDuTempsDTO } from '../../services/emploi-du-temps.service';
import { EcoleService, EcoleDTO } from '../../services/ecole.service';

interface EnseignantDispo {
  enseignantId: string;
  nom?: string;
  prenom?: string;
  email?: string;
  departement?: string;
  nombreDisponibilites: number;
}

interface SlotGrid {
  [classeId: string]: {
    nomClasse: string;
    jours: {
      [jour: number]: {
        [creneau: string]: EmploiDuTempsDTO | null;
      };
    };
  };
}

@Component({
  selector: 'app-generation-emplois',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './generation-emplois.component.html',
  styleUrls: ['./generation-emplois.component.css']
})
export class GenerationEmploisComponent implements OnInit {

  ecoles: EcoleDTO[] = [];
  selectedEcoleId: string = '';
  enseignantsDispo: EnseignantDispo[] = [];

  isLoadingEcoles = false;
  isLoadingEnseignants = false;
  isGenerating = false;

  emploisGeneres: EmploiDuTempsDTO[] = [];
  grille: SlotGrid = {};
  classesIds: string[] = [];

  messages = { success: '', error: '' };

  readonly JOURS = [
    { num: 1, label: 'Lundi' },
    { num: 2, label: 'Mardi' },
    { num: 3, label: 'Mercredi' },
    { num: 4, label: 'Jeudi' },
    { num: 5, label: 'Vendredi' }
  ];

  readonly CRENEAUX = ['8h-10h', '10h-12h', '12h-14h', '14h-16h', '16h-18h'];

  constructor(
    private emploiDuTempsService: EmploiDuTempsService,
    private ecoleService: EcoleService
  ) {}

  ngOnInit(): void {
    this.loadEcoles();
    this.loadEnseignantsDispo();
  }

  loadEcoles(): void {
    this.isLoadingEcoles = true;
    this.ecoleService.obtenirToutes().subscribe({
      next: (ecoles) => {
        this.ecoles = ecoles;
        if (ecoles.length > 0 && !this.selectedEcoleId) {
          this.selectedEcoleId = ecoles[0].idEcole ?? '';
        }
        this.isLoadingEcoles = false;
      },
      error: () => {
        this.messages.error = 'Impossible de charger les écoles.';
        this.isLoadingEcoles = false;
      }
    });
  }

  loadEnseignantsDispo(): void {
    this.isLoadingEnseignants = true;
    this.emploiDuTempsService.getEnseignantsAvecDisponibilites().subscribe({
      next: (data: any[]) => {
        this.enseignantsDispo = data.map(d => ({
          enseignantId: d.enseignantId ?? d.idUser,
          nom: d.nom ?? d.nomUser,
          prenom: d.prenom ?? d.prenomUser,
          email: d.email,
          departement: d.departement,
          nombreDisponibilites: d.nombreDisponibilites ?? 0
        }));
        this.isLoadingEnseignants = false;
      },
      error: () => {
        this.messages.error = 'Impossible de charger la liste des enseignants.';
        this.isLoadingEnseignants = false;
      }
    });
  }

  generer(): void {
    this.isGenerating = true;
    this.messages = { success: '', error: '' };
    this.emploiDuTempsService.genererEmploisDuTemps(this.selectedEcoleId || undefined).subscribe({
      next: (emplois) => {
        this.emploisGeneres = emplois;
        this.buildGrille(emplois);
        this.isGenerating = false;
        this.messages.success = `${emplois.length} créneaux générés avec succès pour la semaine à venir.`;
        setTimeout(() => { this.messages.success = ''; }, 4000);
      },
      error: (err) => {
        this.messages.error = err.error?.message || 'Erreur lors de la génération.';
        this.isGenerating = false;
      }
    });
  }

  private buildGrille(emplois: EmploiDuTempsDTO[]): void {
    this.grille = {};
    this.classesIds = [];

    for (const e of emplois) {
      if (!this.grille[e.classeId]) {
        this.grille[e.classeId] = { nomClasse: e.nomClasse ?? e.classeId, jours: {} };
        this.classesIds.push(e.classeId);
      }
      const entry = this.grille[e.classeId];
      if (!entry.jours[e.jourSemaine]) entry.jours[e.jourSemaine] = {};
      entry.jours[e.jourSemaine][e.creneau] = e;
    }

    this.classesIds.sort((a, b) =>
      (this.grille[a].nomClasse).localeCompare(this.grille[b].nomClasse));
  }

  getSlot(classeId: string, jour: number, creneau: string): EmploiDuTempsDTO | null {
    return this.grille[classeId]?.jours?.[jour]?.[creneau] ?? null;
  }

  getSlotClass(slot: EmploiDuTempsDTO | null): string {
    if (!slot) return 'slot-empty';
    const t = (slot.typeCours ?? '').toLowerCase();
    if (t === 'tpe') return 'slot-tpe';
    if (t === 'cm') return 'slot-cm';
    if (t === 'td') return 'slot-td';
    if (t === 'tp') return 'slot-tp';
    return 'slot-other';
  }

  getSlotLabel(slot: EmploiDuTempsDTO | null): string {
    if (!slot) return '';
    if (slot.typeCours === 'TPE') return 'TPE';
    return `${slot.typeCours ?? ''} – ${slot.codeUE ?? ''}`;
  }

  getSlotTooltip(slot: EmploiDuTempsDTO | null): string {
    if (!slot) return '';
    if (slot.typeCours === 'TPE') return 'Travaux Personnels Encadrés';
    return [
      slot.typeCours,
      slot.codeUE ? `UE: ${slot.codeUE} – ${slot.nomUE ?? ''}` : '',
      slot.enseignantId ? `Prof: ${slot.enseignantId}` : '',
      slot.nomSalle ? `Salle: ${slot.nomSalle}` : ''
    ].filter(Boolean).join('\n');
  }

  get nombreCM(): number { return this.emploisGeneres.filter(e => e.typeCours === 'CM').length; }
  get nombreTD(): number { return this.emploisGeneres.filter(e => e.typeCours === 'TD').length; }
  get nombreTP(): number { return this.emploisGeneres.filter(e => e.typeCours === 'TP').length; }
  get nombreTPE(): number { return this.emploisGeneres.filter(e => e.typeCours === 'TPE').length; }

  get nomEcoleSelectionnee(): string {
    return this.ecoles.find(e => e.idEcole === this.selectedEcoleId)?.nomEcole ?? '';
  }
}
