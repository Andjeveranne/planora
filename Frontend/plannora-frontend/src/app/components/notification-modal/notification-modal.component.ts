import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, NotificationConfig } from '../../services/notification.service';

@Component({
  selector: 'app-notification-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-modal.component.html',
  styleUrls: ['./notification-modal.component.css']
})
export class NotificationModalComponent implements OnInit, OnDestroy {
  config: NotificationConfig | null = null;
  visible = false;
  private unsub: (() => void) | null = null;

  constructor(private notification: NotificationService) {}

  ngOnInit(): void {
    this.unsub = this.notification.subscribe((c, v) => {
      this.config = c;
      this.visible = v;
    });
  }

  ngOnDestroy(): void {
    this.unsub?.();
  }

  onOverlayClick(): void {
    if (this.config?.type === 'confirm') {
      this.notification.onCancel();
    } else {
      this.notification.onOk();
    }
  }

  onOk(): void {
    this.notification.onOk();
  }

  onCancel(): void {
    this.notification.onCancel();
  }

  get iconClass(): string {
    switch (this.config?.type) {
      case 'success': return 'icon-success';
      case 'error': return 'icon-error';
      case 'warning': return 'icon-warning';
      default: return 'icon-info';
    }
  }

  get headerClass(): string {
    switch (this.config?.type) {
      case 'success': return 'header-success';
      case 'error': return 'header-error';
      case 'warning': return 'header-warning';
      default: return 'header-info';
    }
  }
}
