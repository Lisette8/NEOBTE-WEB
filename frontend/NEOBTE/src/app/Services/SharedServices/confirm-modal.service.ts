import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface ConfirmModalOptions {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: 'danger' | 'warning' | 'info';
}

@Injectable({
  providedIn: 'root'
})
export class ConfirmModalService {
  private confirmSubject = new Subject<boolean>();
  private modalOptions = new Subject<ConfirmModalOptions | null>();

  modalOptions$ = this.modalOptions.asObservable();

  constructor() {}

  confirm(options: ConfirmModalOptions): Promise<boolean> {
    this.modalOptions.next(options);
    return new Promise((resolve) => {
      const subscription = this.confirmSubject.subscribe((result) => {
        subscription.unsubscribe();
        resolve(result);
      });
    });
  }


  //private functions
  resolve(result: boolean) {
    this.modalOptions.next(null);
    this.confirmSubject.next(result);
  }
}
