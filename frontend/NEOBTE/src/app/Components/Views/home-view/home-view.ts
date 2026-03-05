import { Component, OnInit } from '@angular/core';
import { ActualiteService } from '../../../Services/actualite-service';
import { Actualite } from '../../../Entities/Interfaces/actualite';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home-view',
  imports: [CommonModule],
  templateUrl: './home-view.html',
  styleUrl: './home-view.css',
})
export class HomeView implements OnInit{
  
  actualites: Actualite[] = [];

  constructor(private actualiteService: ActualiteService) {}
  ngOnInit(): void {
    throw new Error('Method not implemented.');
  }

  nngOnInit(): void {
    this.actualiteService.getAll().subscribe(data => {
      this.actualites = data;
    });
  }
}
