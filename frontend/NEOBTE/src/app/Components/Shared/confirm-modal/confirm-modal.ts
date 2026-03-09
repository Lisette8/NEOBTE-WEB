import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfirmModalService, ConfirmModalOptions } from '../../../Services/SharedServices/confirm-modal.service';

@Component({
  selector: 'app-confirm-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirm-modal.html',
  styleUrl: './confirm-modal.css'
})
export class ConfirmModal implements OnInit {
  options: ConfirmModalOptions | null = null;

  constructor(private modalService: ConfirmModalService) {}

  ngOnInit(): void {
    this.modalService.modalOptions$.subscribe(options => {
      this.options = options;
    });
  }

  confirm(): void {
    this.modalService.resolve(true);
  }

  cancel(): void {
    this.modalService.resolve(false);
  }
}
