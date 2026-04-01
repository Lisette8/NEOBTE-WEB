import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { RevealOnScrollDirective } from '../../../Directives/reveal-on-scroll.directive';

@Component({
  selector: 'app-landing-view',
  standalone: true,
  imports: [RouterLink, RevealOnScrollDirective],
  templateUrl: './landing-view.html',
  styleUrl: './landing-view.css',
})
export class LandingView {

}
