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


  constructor(private actualiteService: ActualiteService) {}

  ngOnInit(): void {
    this.loadActualites();
  }

  loadActualites() {
    this.actualiteService.getAll(this.page, this.size).subscribe(data => {
      this.actualites = data.content;
      this.totalPages = data.totalPages;
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
