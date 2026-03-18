import { Component, OnDestroy, OnInit } from '@angular/core';
import { Actualite } from '../../../Entities/Interfaces/actualite';
import { ActualiteService } from '../../../Services/actualite-service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConfirmModalService } from '../../../Services/SharedServices/confirm-modal.service';
import { ActualiteCreateDTO } from '../../../Entities/DTO/actualite-create-dto';
import { WebsocketService } from '../../../Services/SharedServices/websocket.service';

@Component({
  selector: 'app-actualite-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './actualite-management.html',
  styleUrl: './actualite-management.css',
})
export class ActualiteManagement implements OnInit, OnDestroy {

  actualites: Actualite[] = [];
  page = 0;
  size = 6;
  totalPages = 0;

  form: ActualiteCreateDTO = {
    titre: '',
    sousTitre: '',
    categorie: '',
    description: '',
    contenu: '',
  };

  editingId: number | null = null;
  imageFile: File | null = null;
  imagePreviewUrl: string | null = null;

  constructor(
    private actualiteService: ActualiteService,
    private modalService: ConfirmModalService,
    private ws: WebsocketService
  ) { }

  ngOnInit(): void {
    this.loadActualites();
    this.ws.subscribeAdmin((event) => {
      if (event.type === 'ACTUALITE') this.loadActualites();
    });
  }

  ngOnDestroy() { }

  loadActualites() {
    this.actualiteService.getAll(this.page, this.size).subscribe(data => {
      this.actualites = data.content;
      this.totalPages = data.totalPages;
    });
  }

  submit() {
    const payload: ActualiteCreateDTO = {
      titre: this.form.titre,
      sousTitre: this.form.sousTitre ?? null,
      categorie: this.form.categorie ?? null,
      description: this.form.description,
      contenu: this.form.contenu ?? null,
    };

    if (this.editingId != null) {
      this.actualiteService.update(this.editingId, payload, this.imageFile).subscribe(() => {
        this.resetForm();
        this.loadActualites();
      });
      return;
    }

    this.actualiteService.create(payload, this.imageFile).subscribe(() => {
      this.resetForm();
      this.loadActualites();
    });
  }

  startEdit(act: Actualite) {
    this.editingId = act.idActualite;
    this.form = {
      titre: act.titre,
      sousTitre: act.sousTitre ?? '',
      categorie: act.categorie ?? '',
      description: act.description ?? '',
      contenu: act.contenu ?? '',
    };
    this.imageFile = null;
    this.imagePreviewUrl = act.imageUrl ? this.mediaUrl(act.imageUrl) : null;
  }

  cancelEdit() { this.resetForm(); }

  onImageSelected(ev: Event) {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.imageFile = file;
    this.imagePreviewUrl = file ? URL.createObjectURL(file) : null;
  }

  resetForm() {
    this.editingId = null;
    this.form = { titre: '', sousTitre: '', categorie: '', description: '', contenu: '' };
    this.imageFile = null;
    this.imagePreviewUrl = null;
  }

  mediaUrl(url?: string | null): string {
    if (!url) return '';
    if (url.startsWith('http')) return url;
    return `http://localhost:8080${url}`;
  }

  async deleteActualite(id: number) {
    const confirmed = await this.modalService.confirm({
      title: 'Supprimer l\'actualité',
      message: 'Êtes-vous sûr de vouloir supprimer cette actualité ? Cette action est irréversible.',
      confirmText: 'Supprimer',
      cancelText: 'Annuler',
      type: 'danger'
    });

    if (confirmed) {
      this.actualiteService.delete(id).subscribe(() => {
        this.loadActualites();
      });
    }
  }

  nextPage() {
    if (this.page < this.totalPages - 1) { this.page++; this.loadActualites(); }
  }

  previousPage() {
    if (this.page > 0) { this.page--; this.loadActualites(); }
  }
}