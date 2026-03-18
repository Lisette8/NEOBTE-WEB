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

  readonly reactions = [
    // "Sérieux" façon LinkedIn (icône + libellé), sans réactions moqueuses/tristes/colère
    { key: 'LIKE', emoji: '👍', label: "J’aime" },
    { key: 'CELEBRATE', emoji: '👏', label: 'Bravo' },
    { key: 'SUPPORT', emoji: '🤝', label: 'Soutien' },
    { key: 'LOVE', emoji: '❤️', label: "J’adore" },
    { key: 'INSIGHTFUL', emoji: '💡', label: 'Pertinent' },
    { key: 'CURIOUS', emoji: '🤔', label: 'Intéressant' },
  ] as const;

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
        this.error = 'Impossible de charger les actualités.';
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

  mediaUrl(url?: string | null): string {
    if (!url) return '';
    if (url.startsWith('http')) return url;
    return `http://localhost:8080${url}`;
  }

  react(act: Actualite, reaction: string) {
    this.actualiteService.react(act.idActualite, reaction).subscribe({
      next: (updated) => {
        act.reactions = updated.reactions ?? {};
        act.myReaction = updated.myReaction ?? null;
      },
      error: () => {}
    });
  }

  reactionCount(act: Actualite, reaction: string): number {
    return Number((act.reactions ?? {})[reaction] ?? 0);
  }
}
