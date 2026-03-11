import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslationService } from '../../Services/SharedServices/translation-service';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './footer.html',
  styleUrl: './footer.css',
})
export class Footer {
  constructor(public transService: TranslationService) {}
}
