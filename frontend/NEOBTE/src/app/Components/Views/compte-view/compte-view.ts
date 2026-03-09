import { Component, OnInit } from '@angular/core';
import { Compte } from '../../../Entities/Interfaces/compte';
import { CompteService } from '../../../Services/compte-service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-compte-view',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './compte-view.html',
  styleUrl: './compte-view.css',
})
export class CompteView implements OnInit {
  
  comptes: Compte[] = [];

  constructor(private compteService: CompteService) {}
  
  ngOnInit(): void {
    this.loadAccounts();
  }



  loadAccounts(){
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const userId = user.id;

    this.compteService.getUserAccounts(userId)
    .subscribe(data => {
      this.comptes = data;
    })
  }


}
