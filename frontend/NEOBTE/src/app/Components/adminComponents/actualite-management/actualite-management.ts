import { Component, OnInit } from '@angular/core';
import { Actualite } from '../../../Entities/Interfaces/actualite';
import { ActualiteService } from '../../../Services/actualite-service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConfirmModalService } from '../../../Services/SharedServices/confirm-modal.service';

@Component({
  selector: 'app-actualite-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './actualite-management.html',
  styleUrl: './actualite-management.css',
})
export class ActualiteManagement implements OnInit {


  actualites: Actualite[] = [];
  page = 0;
  size = 6;
  totalPages = 0;

  titre = '';
  description = '';

  editingId: number | null = null;
  editTitre = '';
  editDescription = '';

  constructor(
    private actualiteService: ActualiteService,
    private modalService: ConfirmModalService
  ) { }

  ngOnInit(): void {
    this.loadActualites();
  }


  loadActualites() {
    this.actualiteService.getAll(this.page, this.size).subscribe(data => {
      this.actualites = data.content;
      this.totalPages = data.totalPages;
    });
  }


  createActualite() {

    const data = {
      titre: this.titre,
      description: this.description
    };

    this.actualiteService.create(data).subscribe(() => {

      this.titre = '';
      this.description = '';

      this.loadActualites();
    });
  }

  startEdit(act: Actualite) {
    this.editingId = act.idActualite;
    this.editTitre = act.titre;
    this.editDescription = act.description;
  }

  cancelEdit() {
    this.editingId = null;
    this.editTitre = '';
    this.editDescription = '';
  }

  updateActualite(id: number) {

    const data = {
      titre: this.editTitre,
      description: this.editDescription
    };

    this.actualiteService.update(id, data).subscribe(() => {

      this.editingId = null;
      this.editTitre = '';
      this.editDescription = '';

      this.loadActualites();
    });
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


  //pagination functions
  nextPage() {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.loadActualites();
    }
  }

  previousPage() {
    if (this.page > 0) {
      this.page--;
      this.loadActualites();
    }
  }



}
