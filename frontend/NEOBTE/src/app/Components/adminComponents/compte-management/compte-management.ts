import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { CompteService } from '../../../Services/compte-service';
import { Compte } from '../../../Entities/Interfaces/compte';

@Component({
  selector: 'app-compte-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './compte-management.html',
  styleUrl: './compte-management.css',
})
export class CompteManagement implements OnInit{

  comptes: Compte[] = [];  
  
  constructor(
    private compteService: CompteService
  ){}

  ngOnInit(): void {
    this.loadAccounts();
  }


  loadAccounts(){
    this.compteService.getAllAccounts().subscribe(
      data => { this.comptes = data}
    )
  }


  deleteAccount(id: number){
    if(confirm('Are you sure you want to delete this account?')){
      this.compteService.deleteAccount(id).subscribe(
        () => {
          this.loadAccounts();
        }
      )
    }
  }
  

}
