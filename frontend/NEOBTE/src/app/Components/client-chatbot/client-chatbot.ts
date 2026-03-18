import { CommonModule } from '@angular/common';
import { Component, ElementRef, Input, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ClientAiService } from '../../Services/client-ai.service';
import { ClientChatMessage } from '../../Entities/Interfaces/client-premium';

@Component({
  selector: 'app-client-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './client-chatbot.html',
  styleUrl: './client-chatbot.css',
})
export class ClientChatbot {
  @Input() isPremium = false;

  @ViewChild('chatScroll') chatScrollRef!: ElementRef<HTMLDivElement>;

  messages: ClientChatMessage[] = [];
  inputMessage = '';
  chatLoading = false;
  chatError = '';

  readonly suggestedQuestions = [
    'Comment mieux gérer mon budget mensuel ?',
    'Des conseils pour réduire mes dépenses ?',
    'Quelles sont les bonnes pratiques pour les virements ?',
    'Explique-moi l’inflation et son impact.',
    'Comment profiter au mieux de NeoBTE Premium ?',
  ];

  constructor(private aiService: ClientAiService) {}

  useSuggestion(q: string) {
    this.inputMessage = q;
    this.sendMessage();
  }

  sendMessage() {
    const message = this.inputMessage.trim();
    if (!message || this.chatLoading) return;

    if (!this.isPremium) {
      this.chatError = 'Cette fonctionnalité est réservée aux abonnés Premium.';
      return;
    }

    this.chatError = '';
    this.chatLoading = true;
    this.messages.push({ role: 'user', content: message });
    this.inputMessage = '';
    this.scrollToBottom();

    const history = this.messages
      .slice(0, -1) // exclude last user message (we pass it separately)
      .map(m => ({ role: m.role, content: m.content }));

    this.aiService.chat(message, history).subscribe({
      next: (res) => {
        this.messages.push({ role: 'assistant', content: res.reply });
        this.chatLoading = false;
        this.scrollToBottom();
      },
      error: (err) => {
        this.chatLoading = false;
        this.chatError = err?.error?.message || 'AI indisponible. Vérifiez GROQ_API_KEY côté backend.';
      },
    });
  }

  private scrollToBottom() {
    setTimeout(() => {
      const el = this.chatScrollRef?.nativeElement;
      if (!el) return;
      el.scrollTop = el.scrollHeight;
    }, 30);
  }
}
