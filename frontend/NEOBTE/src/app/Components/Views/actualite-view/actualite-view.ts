import { Component, OnInit } from '@angular/core';
import { Actualite } from '../../../Entities/Interfaces/actualite';
import { ActualiteService } from '../../../Services/actualite-service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-actualite-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './actualite-view.html',
  styleUrl: './actualite-view.css',
})
export class ActualiteView implements OnInit {

  actualites: Actualite[] = [];
  page = 0;
  size = 5;
  totalPages = 0;
  loading = false;
  error = '';

  constructor(private actualiteService: ActualiteService) {}

  ngOnInit(): void {
    this.loadActualites();
  }

  loadActualites() {
    this.loading = true;
    this.error = '';
    this.actualiteService.getAll(this.page, this.size).subscribe({
      next: (data) => {
        this.actualites = data.content;
        this.totalPages = data.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load news.';
        this.loading = false;
      }
    });
  }

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
