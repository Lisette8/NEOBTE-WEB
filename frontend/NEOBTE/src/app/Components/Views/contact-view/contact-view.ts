import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';

declare const L: any;  // Leaflet — loaded via CDN in index.html

interface Agency {
  name: string;
  address: string;
  phone?: string;
  lat: number;
  lng: number;
}

@Component({
  selector: 'app-contact-view',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './contact-view.html',
  styleUrl: './contact-view.css',
})
export class ContactView implements AfterViewInit, OnDestroy {

  // ── Contact form state ──────────────────────────────────────────────
  form = { nom: '', email: '', sujet: '', message: '' };
  sending = false;
  success = '';
  error = '';

  // ── Map ─────────────────────────────────────────────────────────────
  private map: any;
  activeAgency: Agency | null = null;

  readonly agencies: Agency[] = [
    { name: 'Siège Social — Centre Urbain Nord',  address: 'Bld Beji Caid Essebsi, Lot AFH–BC8, 1082 Tunis', phone: '+216 71 112 000', lat: 36.8334, lng: 10.1912 },
    { name: 'Agence Tunis Centre',                address: 'Avenue Habib Bourguiba, Tunis',                   phone: '+216 71 334 000', lat: 36.7992, lng: 10.1797 },
    { name: 'Agence Les Berges du Lac',           address: 'Rue du Lac Malaren, Les Berges du Lac, Tunis',    phone: '+216 71 960 100', lat: 36.8448, lng: 10.2297 },
    { name: 'Agence Ariana',                      address: 'Avenue de la République, Ariana',                 phone: '+216 71 705 100', lat: 36.8605, lng: 10.1927 },
    { name: 'Agence Ben Arous',                   address: 'Avenue Habib Bourguiba, Ben Arous',               phone: '+216 71 383 100', lat: 36.7528, lng: 10.2278 },
    { name: 'Agence Sousse',                      address: 'Avenue Habib Bourguiba, Sousse',                  phone: '+216 73 220 100', lat: 35.8256, lng: 10.6369 },
    { name: 'Agence Sfax',                        address: 'Avenue Hedi Chaker, Sfax',                        phone: '+216 74 241 100', lat: 34.7406, lng: 10.7603 },
    { name: 'Agence Nabeul',                      address: 'Avenue Habib Thameur, Nabeul',                    phone: '+216 72 285 100', lat: 36.4561, lng: 10.7376 },
    { name: 'Agence Monastir',                    address: 'Avenue Habib Bourguiba, Monastir',                phone: '+216 73 462 100', lat: 35.7643, lng: 10.8113 },
    { name: 'Agence Bizerte',                     address: 'Avenue Habib Bourguiba, Bizerte',                 phone: '+216 72 431 100', lat: 37.2744, lng: 9.8739  },
  ];

  constructor(private http: HttpClient) {}

  ngAfterViewInit(): void {
    this.initMap();
  }

  ngOnDestroy(): void {
    if (this.map) { this.map.remove(); this.map = null; }
  }

  private initMap() {
    if (typeof L === 'undefined') {
      // Leaflet not loaded yet — retry after a short delay
      setTimeout(() => this.initMap(), 300);
      return;
    }

    this.map = L.map('bte-map', { zoomControl: true, scrollWheelZoom: false })
               .setView([36.5, 10.2], 7);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      maxZoom: 18,
    }).addTo(this.map);

    const icon = L.divIcon({
      className: '',
      html: `<div class="map-pin"><svg viewBox="0 0 24 24" width="28" height="28" fill="#0000A0">
               <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
             </svg></div>`,
      iconSize: [28, 28],
      iconAnchor: [14, 28],
      popupAnchor: [0, -30],
    });

    this.agencies.forEach(agency => {
      L.marker([agency.lat, agency.lng], { icon })
       .addTo(this.map)
       .bindPopup(`
         <div class="map-popup">
           <strong>${agency.name}</strong><br>
           <span>${agency.address}</span><br>
           ${agency.phone ? `<a href="tel:${agency.phone}">${agency.phone}</a>` : ''}
         </div>`)
       .on('click', () => { this.activeAgency = agency; });
    });
  }

  flyTo(agency: Agency) {
    this.activeAgency = agency;
    if (this.map) this.map.flyTo([agency.lat, agency.lng], 15, { duration: 1.2 });
  }

  // ── Form submit ──────────────────────────────────────────────────────
  submit() {
    this.error = ''; this.success = '';
    if (!this.form.nom.trim() || !this.form.email.trim() ||
        !this.form.sujet.trim() || !this.form.message.trim()) {
      this.error = 'Veuillez remplir tous les champs.'; return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.form.email)) {
      this.error = 'Adresse e-mail invalide.'; return;
    }
    this.sending = true;
    this.http.post<{ message: string }>('http://localhost:8080/api/v1/public/contact', this.form).subscribe({
      next: (res) => {
        this.success = res.message;
        this.form = { nom: '', email: '', sujet: '', message: '' };
        this.sending = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Envoi échoué. Veuillez réessayer.';
        this.sending = false;
      }
    });
  }
}
