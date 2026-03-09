import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { CompteService } from '../../../Services/compte-service';
import { Compte } from '../../../Entities/Interfaces/compte';
import { ConfirmModalService } from '../../../Services/SharedServices/confirm-modal.service';

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
    private compteService: CompteService,
    private modalService: ConfirmModalService
  ){}

  ngOnInit(): void {
    this.loadAccounts();
  }


  loadAccounts(){
    this.compteService.getAllAccounts().subscribe(
      data => { this.comptes = data}
    )
  }


  async deleteAccount(id: number){
    const confirmed = await this.modalService.confirm({
      title: 'Supprimer le compte',
      message: 'Êtes-vous sûr de vouloir supprimer ce compte ? Cette action est irréversible.',
      confirmText: 'Supprimer',
      cancelText: 'Annuler',
      type: 'danger'
    });

    if(confirmed){
      this.compteService.deleteAccount(id).subscribe(
        () => {
          this.loadAccounts();
        }
      )
    }
  }
  

}
