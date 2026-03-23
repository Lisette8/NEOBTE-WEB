import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Support } from '../../../Entities/Interfaces/support';
import { SupportService } from '../../../Services/support-service';
import { WebsocketService } from '../../../Services/SharedServices/websocket.service';

type Tab = 'open' | 'in_progress' | 'resolved' | 'all';

@Component({
  selector: 'app-admin-support',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-support.html',
  styleUrl: './admin-support.css',
})
export class AdminSupport implements OnInit, OnDestroy {

  allTickets: Support[] = [];
  responseText: { [key: number]: string } = {};
  statusSelected: { [key: number]: string } = {};
  aiLoading: { [key: number]: boolean } = {};
  expandedTicket: number | null = null;

  activeTab: Tab = 'open';
  searchQuery = '';

  constructor(
    private supportService: SupportService,
    private websocket: WebsocketService
  ) {}

  ngOnInit() {
    this.loadTickets();
    this.websocket.connect((ticket: Support) => {
      const exists = this.allTickets.some(t => t.idSupport === ticket.idSupport);
      if (!exists) this.allTickets = [ticket, ...this.allTickets];
    });
  }

  ngOnDestroy() { this.websocket.disconnect(); }

  loadTickets() {
    this.supportService.getAllTickets().subscribe(data => {
      this.allTickets = data.sort((a, b) =>
        new Date(b.dateCreation).getTime() - new Date(a.dateCreation).getTime()
      );
    });
  }

  // ── Tabs & filtering ────────────────────────────────────────────────────────

  get tabs(): { id: Tab; label: string; count: number }[] {
    return [
      { id: 'open',        label: 'Nouveaux',   count: this.countByStatus('OPEN') },
      { id: 'in_progress', label: 'En cours',   count: this.countByStatus('IN_PROGRESS') },
      { id: 'resolved',    label: 'Résolus',    count: this.countByStatus('RESOLVED') + this.countByStatus('CLOSED') },
      { id: 'all',         label: 'Tous',       count: this.allTickets.length },
    ];
  }

  get visibleTickets(): Support[] {
    let list = this.allTickets;

    // Filter by tab
    if (this.activeTab === 'open')        list = list.filter(t => t.status === 'OPEN');
    else if (this.activeTab === 'in_progress') list = list.filter(t => t.status === 'IN_PROGRESS');
    else if (this.activeTab === 'resolved') list = list.filter(t => t.status === 'RESOLVED' || t.status === 'CLOSED');

    // Filter by search
    const q = this.searchQuery.trim().toLowerCase();
    if (q) {
      list = list.filter(t =>
        t.sujet?.toLowerCase().includes(q) ||
        t.message?.toLowerCase().includes(q) ||
        t.clientEmail?.toLowerCase().includes(q) ||
        t.guestEmail?.toLowerCase().includes(q) ||
        t.guestName?.toLowerCase().includes(q)
      );
    }
    return list;
  }

  private countByStatus(s: string): number {
    return this.allTickets.filter(t => t.status === s).length;
  }

  // ── Stats ────────────────────────────────────────────────────────────────────

  get stats() {
    const total    = this.allTickets.length;
    const open     = this.countByStatus('OPEN');
    const progress = this.countByStatus('IN_PROGRESS');
    const resolved = this.countByStatus('RESOLVED') + this.countByStatus('CLOSED');
    const guests   = this.allTickets.filter(t => t.guest).length;
    return { total, open, progress, resolved, guests };
  }

  // ── Ticket actions ────────────────────────────────────────────────────────────

  toggleExpand(id: number) {
    this.expandedTicket = this.expandedTicket === id ? null : id;
  }

  updateTicket(ticket: Support) {
    const response = this.responseText[ticket.idSupport] ?? '';
    const status = this.statusSelected[ticket.idSupport] || ticket.status;
    this.supportService.updateTicket(ticket.idSupport, response, status).subscribe(() => {
      this.loadTickets();
      this.expandedTicket = null;
    });
  }

  deleteTicket(id: number) {
    this.supportService.deleteTicket(id).subscribe(() => this.loadTickets());
  }

  // ── AI suggest ────────────────────────────────────────────────────────────────

  suggestWithAi(ticket: Support) {
    this.aiLoading[ticket.idSupport] = true;
    const senderName = ticket.guest
      ? (ticket.guestName ?? 'visiteur')
      : (ticket.clientEmail ?? 'client');

    this.supportService.aiSuggest(ticket.sujet, ticket.message, senderName).subscribe({
      next: (res) => {
        this.responseText[ticket.idSupport] = res.suggestion;
        this.aiLoading[ticket.idSupport] = false;
        // Auto-expand if not already open
        if (this.expandedTicket !== ticket.idSupport) this.expandedTicket = ticket.idSupport;
      },
      error: () => { this.aiLoading[ticket.idSupport] = false; }
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────────

  senderLabel(ticket: Support): string {
    return ticket.guest
      ? `${ticket.guestName ?? ''} — ${ticket.guestEmail ?? ''}`
      : (ticket.clientEmail ?? '');
  }

  statusLabel(s: string): string {
    switch (s) {
      case 'OPEN':        return 'Nouveau';
      case 'IN_PROGRESS': return 'En cours';
      case 'RESOLVED':    return 'Résolu';
      case 'CLOSED':      return 'Fermé';
      default:            return s;
    }
  }
}
