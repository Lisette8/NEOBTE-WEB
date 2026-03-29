import { CommonModule } from '@angular/common';
import { Component, HostListener, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ClientAiService } from '../../Services/client-ai.service';
import { ClientChatbot } from '../client-chatbot/client-chatbot';

@Component({
  selector: 'app-client-chatbot-bubble',
  standalone: true,
  imports: [CommonModule, RouterLink, ClientChatbot],
  templateUrl: './client-chatbot-bubble.html',
  styleUrl: './client-chatbot-bubble.css',
})
export class ClientChatbotBubble implements OnInit {
  open = false;
  premium = false;
  loading = true;

  constructor(private clientAiService: ClientAiService) {}

  ngOnInit(): void {
    this.clientAiService.getPremiumStatus().subscribe({
      next: (s) => {
        this.premium = !!s?.premium;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }

  toggle() {
    this.open = !this.open;
  }

  close() {
    this.open = false;
  }

  @HostListener('document:keydown.escape')
  onEsc() {
    this.open = false;
  }
}

