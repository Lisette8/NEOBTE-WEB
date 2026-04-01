import { CommonModule } from '@angular/common';
import { Component, ElementRef, HostListener, OnInit } from '@angular/core';
import { ClientAiService } from '../../Services/client-ai.service';
import { ClientChatbot } from '../client-chatbot/client-chatbot';

@Component({
  selector: 'app-client-chatbot-bubble',
  standalone: true,
  imports: [CommonModule, ClientChatbot],
  templateUrl: './client-chatbot-bubble.html',
  styleUrl: './client-chatbot-bubble.css',
})
export class ClientChatbotBubble implements OnInit {
  open = false;
  isClosing = false;
  premium = false;
  loading = true;

  constructor(private clientAiService: ClientAiService, private elRef: ElementRef) {}

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
    if (this.open) {
      this.animatedClose();
    } else {
      this.isClosing = false;
      this.open = true;
    }
  }

  close() {
    this.animatedClose();
  }

  private animatedClose() {
    this.isClosing = true;
    const panel = this.elRef.nativeElement.querySelector('.cbubble-panel');
    if (!panel) { this.open = false; this.isClosing = false; return; }
    const onEnd = () => {
      panel.removeEventListener('animationend', onEnd);
      this.open = false;
      this.isClosing = false;
    };
    panel.addEventListener('animationend', onEnd);
  }

  @HostListener('document:keydown.escape')
  onEsc() {
    this.open = false;
  }
}

