import { Component, OnInit } from '@angular/core';
import { ActualiteService } from '../../../Services/actualite-service';
import { Actualite } from '../../../Entities/Interfaces/actualite';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home-view',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home-view.html',
  styleUrl: './home-view.css',
})
export class HomeView implements OnInit {

  actualites: Actualite[] = [];

  page = 0;
  size = 5;

  constructor(private actualiteService: ActualiteService) {}

  ngOnInit(): void {
    this.loadActualites();
  }

  loadActualites() {
    this.actualiteService.getAll(this.page, this.size).subscribe(data => {
      this.actualites = data.content;
    });
  }
}