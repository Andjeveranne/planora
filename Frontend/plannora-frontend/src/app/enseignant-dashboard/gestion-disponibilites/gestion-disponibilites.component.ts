import { Component, OnInit } from '@angular/core';
import { CommonModule, NgFor, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { PlanningService, DisponibiliteDTO } from '../../services/planning.service';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { EcoleDTO, EcoleService } from '../../services/ecole.service';

interface TimeSlot {
    time: string;
    startHour: number;
}

interface Day {
    name: string;
    date: Date;
}

@Component({
    selector: 'app-gestion-disponibilites',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './gestion-disponibilites.component.html',
    styleUrl: './gestion-disponibilites.component.css'
})
export class GestionDisponibilitesComponent implements OnInit {
    ecoles: EcoleDTO[] = [];
    selectedEcoleId = '';

    weekDays: Day[] = [];
    timeSlots: TimeSlot[] = [];

    currentWeekStart: Date = new Date();

    // Disponibilités sauvegardées en base
    disponibilitesMap: Map<string, DisponibiliteDTO> = new Map();

    // Sélections en attente de confirmation (ajouts)
    pendingAdditions: Set<string> = new Set();
    // Suppressions en attente de confirmation
    pendingDeletions: Set<string> = new Set();

    enseignantId: string = "";
    loading: boolean = false;
    saving: boolean = false;
    successMessage: string = "";
    errorMessage: string = "";

    constructor(
        private planningService: PlanningService,
        private authService: AuthService,
        private notification: NotificationService,
        private ecoleService: EcoleService
    ) {
        this.initTimeSlots();
    }

    ngOnInit(): void {
        const user = this.authService.getCurrentUser();
        if (user) {
            this.enseignantId = user.userId.toString();
        }

        this.setWeekToNext();
        this.ecoleService.obtenirToutes().subscribe(ecoles => {
            this.ecoles = ecoles;
            if (!this.selectedEcoleId) this.selectedEcoleId = ecoles[0]?.idEcole ?? '';
        });
        this.loadDisponibilites();
    }

    initTimeSlots() {
        // Créneaux de 2h : 8h-10h, 10h-12h, 12h-14h, 14h-16h, 16h-18h
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

    setWeekToNext() {
        const now = new Date();
        const day = now.getDay();
        const diff = now.getDate() - day + (day === 0 ? -6 : 1);
        this.currentWeekStart = new Date(now.getFullYear(), now.getMonth(), diff + 7);
        this.currentWeekStart.setHours(0, 0, 0, 0);
        this.updateWeekDays();
    }

    changeWeek(offset: number) {
        if (this.hasPendingChanges()) {
            this.notification.confirm(
                'Vous avez des modifications non sauvegardées. Voulez-vous les abandonner ?',
                'Modifications non sauvegardées',
                () => {
                    this.clearPendingChanges();
                    this.doChangeWeek(offset);
                }
            );
            return;
        }
        this.doChangeWeek(offset);
    }

    private doChangeWeek(offset: number): void {
        this.clearPendingChanges();
        const newDate = new Date(this.currentWeekStart);
        newDate.setDate(newDate.getDate() + (offset * 7));
        this.currentWeekStart = newDate;
        this.updateWeekDays();
        this.loadDisponibilites();
    }

    updateWeekDays() {
        this.weekDays = [];
        const days = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi'];

        for (let i = 0; i < 5; i++) {
            const date = new Date(this.currentWeekStart);
            date.setDate(date.getDate() + i);
            this.weekDays.push({
                name: days[i],
                date: date
            });
        }
    }

    loadDisponibilites() {
        if (!this.enseignantId) return;

        this.loading = true;
        this.planningService.getDisponibilites(this.enseignantId).subscribe({
            next: (data) => {
                this.disponibilitesMap.clear();
                data.forEach(d => {
                    const start = new Date(d.dateHeureDebut);
                    const key = this.getKey(start);
                    this.disponibilitesMap.set(key, d);
                });
                this.loading = false;
            },
            error: (err) => {
                console.error("Erreur chargement dispos", err);
                this.loading = false;
                this.showError("Erreur lors du chargement des disponibilités");
            }
        });
    }

    getKey(date: Date): string {
        const y = date.getFullYear();
        const m = (date.getMonth() + 1).toString().padStart(2, '0');
        const d = date.getDate().toString().padStart(2, '0');
        const h = date.getHours().toString().padStart(2, '0');
        return `${y}-${m}-${d}_${h}`;
    }

    getKeyForSlot(day: Day, slot: TimeSlot): string {
        const date = new Date(day.date);
        date.setHours(slot.startHour, 0, 0, 0);
        return this.getKey(date);
    }

    // Vérifie si un jour est passé (avant aujourd'hui)
    isPastDay(day: Day): boolean {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const dayDate = new Date(day.date);
        dayDate.setHours(0, 0, 0, 0);
        return dayDate < today;
    }

    // Vérifie si un créneau spécifique est passé (jour passé uniquement, pas l'heure)
    isPastSlot(day: Day, slot: TimeSlot): boolean {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const dayDate = new Date(day.date);
        dayDate.setHours(0, 0, 0, 0);
        return dayDate < today;
    }

    // Vérifie si le slot est disponible (sauvegardé OU en attente d'ajout, SAUF si en attente de suppression)
    isAvailable(day: Day, slot: TimeSlot): boolean {
        const key = this.getKeyForSlot(day, slot);

        // Si en attente de suppression, montrer comme non disponible
        if (this.pendingDeletions.has(key)) {
            return false;
        }

        // Si en attente d'ajout, montrer comme disponible
        if (this.pendingAdditions.has(key)) {
            return true;
        }

        // Sinon, vérifier en base
        return this.disponibilitesMap.has(key);
    }

    // Vérifie si le slot a des modifications en attente
    isPending(day: Day, slot: TimeSlot): boolean {
        const key = this.getKeyForSlot(day, slot);
        return this.pendingAdditions.has(key) || this.pendingDeletions.has(key);
    }

    // Toggle la sélection (ne sauvegarde pas directement)
    toggleAvailability(day: Day, slot: TimeSlot) {
        if (this.loading || this.saving) return;

        // Empêcher les modifications sur les jours passés
        if (this.isPastSlot(day, slot)) {
            this.showError("Impossible de modifier les disponibilités passées");
            return;
        }

        const key = this.getKeyForSlot(day, slot);
        this.clearMessages();

        // Cas 1: Le slot est déjà en base
        if (this.disponibilitesMap.has(key)) {
            if (this.pendingDeletions.has(key)) {
                // Annuler la suppression
                this.pendingDeletions.delete(key);
            } else {
                // Marquer pour suppression
                this.pendingDeletions.add(key);
            }
        }
        // Cas 2: Le slot n'est pas en base
        else {
            if (this.pendingAdditions.has(key)) {
                // Annuler l'ajout
                this.pendingAdditions.delete(key);
            } else {
                // Marquer pour ajout
                this.pendingAdditions.add(key);
            }
        }
    }

    // Vérifie s'il y a des changements en attente
    hasPendingChanges(): boolean {
        return this.pendingAdditions.size > 0 || this.pendingDeletions.size > 0;
    }

    // Annule tous les changements en attente
    clearPendingChanges() {
        this.pendingAdditions.clear();
        this.pendingDeletions.clear();
        this.clearMessages();
    }

    // Confirme et sauvegarde tous les changements
    confirmChanges() {
        if (!this.hasPendingChanges()) return;

        this.saving = true;
        this.clearMessages();

        const addObservables: any[] = [];
        const deleteObservables: any[] = [];

        // Traiter les ajouts
        this.pendingAdditions.forEach(key => {
            const [datePart, hourPart] = key.split('_');
            const [year, month, day] = datePart.split('-').map(Number);
            const hour = parseInt(hourPart);

            // Durée fixe : 2h par créneau
            const duration = 2;

            const dStart = new Date(year, month - 1, day, hour, 0, 0, 0);
            const dEnd = new Date(year, month - 1, day, hour + duration, 0, 0, 0);

            const newDispo: DisponibiliteDTO = {
                enseignantId: this.enseignantId,
                idEcole: this.selectedEcoleId,
                dateHeureDebut: dStart.toISOString(),
                dateHeureFin: dEnd.toISOString()
            };

            addObservables.push(this.planningService.addDisponibilite(newDispo));
        });

        // Traiter les suppressions
        this.pendingDeletions.forEach(key => {
            const dispo = this.disponibilitesMap.get(key);
            if (dispo && dispo.id) {
                deleteObservables.push(this.planningService.deleteDisponibilite(dispo.id));
            }
        });

        const allOps = [...addObservables, ...deleteObservables];
        if (allOps.length === 0) {
            this.saving = false;
            this.clearPendingChanges();
            return;
        }

        forkJoin(allOps).subscribe({
            next: () => {
                this.pendingAdditions.clear();
                this.pendingDeletions.clear();
                this.saving = false;
                this.notification.success(
                    `✅ Disponibilités enregistrées avec succès ! L'administrateur peut maintenant les consulter.`
                );
                this.loadDisponibilites();
            },
            error: (err) => {
                console.error("Erreur lors de la sauvegarde", err);
                this.saving = false;
                const msg = err?.error?.message || err?.message || 'Erreur lors de la sauvegarde.';
                this.notification.error(`❌ ${msg} Veuillez réessayer.`);
                this.loadDisponibilites();
            }
        });
    }

    showSuccess(message: string) {
        this.successMessage = message;
        setTimeout(() => this.successMessage = "", 4000);
    }

    showError(message: string) {
        this.errorMessage = message;
        setTimeout(() => this.errorMessage = "", 4000);
    }

    clearMessages() {
        this.successMessage = "";
        this.errorMessage = "";
    }

    // Compte le nombre de changements en attente
    getPendingChangesCount(): number {
        return this.pendingAdditions.size + this.pendingDeletions.size;
    }
}
