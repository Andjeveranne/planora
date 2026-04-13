import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface NotificationConfig {
  message: string;
  title?: string;
  type: 'success' | 'error' | 'warning' | 'info' | 'confirm';
  confirmText?: string;
  cancelText?: string;
  onOk?: () => void;
  onCancel?: () => void;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationSubject = new Subject<NotificationConfig>();
  private currentConfig: NotificationConfig | null = null;

  success(message: string, title?: string): void {
    this.showNotification(message, 'success', title);
  }

  error(message: string, title?: string): void {
    this.showNotification(message, 'error', title);
  }

  warning(message: string, title?: string): void {
    this.showNotification(message, 'warning', title);
  }

  info(message: string, title?: string): void {
    this.showNotification(message, 'info', title);
  }

  confirm(message: string, title?: string, onOk?: () => void, onCancel?: () => void): void {
    // Pour l'instant, utiliser confirm() natif
    const result = confirm(message);
    if (result && onOk) {
      onOk();
    } else if (!result && onCancel) {
      onCancel();
    }
  }

  subscribe(callback: (config: NotificationConfig, visible: boolean) => void) {
    return this.notificationSubject.subscribe((config) => {
      this.currentConfig = config;
      callback(config, true);
    });
  }

  onOk(): void {
    if (this.currentConfig?.onOk) {
      this.currentConfig.onOk();
    }
    this.currentConfig = null;
  }

  onCancel(): void {
    if (this.currentConfig?.onCancel) {
      this.currentConfig.onCancel();
    }
    this.currentConfig = null;
  }

  private showNotification(message: string, type: 'success' | 'error' | 'warning' | 'info', title?: string): void {
    // Simple alert pour l'instant, peut être remplacé par une notification plus sophistiquée
    const prefix = type === 'error' ? '❌ ' : 
                   type === 'success' ? '✅ ' : 
                   type === 'warning' ? '⚠️ ' : 
                   'ℹ️ ';
    
    const displayMessage = title ? `${title}: ${message}` : message;
    alert(prefix + displayMessage);
  }
}