import { Injectable } from '@angular/core';

export type NotificationType = 'success' | 'error' | 'info' | 'confirm' | 'warning';

export interface NotificationConfig {
  type: NotificationType;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm?: () => void;
  onCancel?: () => void;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private config: NotificationConfig | null = null;
  private visible = false;
  private listeners: ((c: NotificationConfig | null, v: boolean) => void)[] = [];

  getConfig(): NotificationConfig | null {
    return this.config;
  }

  isVisible(): boolean {
    return this.visible;
  }

  subscribe(fn: (c: NotificationConfig | null, v: boolean) => void): () => void {
    this.listeners.push(fn);
    return () => {
      this.listeners = this.listeners.filter(l => l !== fn);
    };
  }

  private notify(): void {
    this.listeners.forEach(fn => fn(this.config, this.visible));
  }

  private show(type: NotificationType, title: string, message: string, options?: Partial<NotificationConfig>): void {
    this.config = {
      type,
      title,
      message,
      confirmText: options?.confirmText ?? 'OK',
      cancelText: options?.cancelText ?? 'Annuler',
      onConfirm: options?.onConfirm,
      onCancel: options?.onCancel,
    };
    this.visible = true;
    this.notify();
  }

  success(message: string, title = 'Succès'): void {
    this.show('success', title, message);
  }

  error(message: string, title = 'Erreur'): void {
    this.show('error', title, message);
  }

  info(message: string, title = 'Information'): void {
    this.show('info', title, message);
  }

  warning(message: string, title = 'Attention'): void {
    this.show('warning', title, message);
  }

  confirm(message: string, title = 'Confirmation', onConfirm?: () => void, onCancel?: () => void): void {
    this.show('confirm', title, message, { onConfirm, onCancel });
  }

  close(): void {
    this.visible = false;
    this.config = null;
    this.notify();
  }

  onOk(): void {
    const c = this.config;
    this.close();
    c?.onConfirm?.();
  }

  onCancel(): void {
    const c = this.config;
    this.close();
    c?.onCancel?.();
  }
}
