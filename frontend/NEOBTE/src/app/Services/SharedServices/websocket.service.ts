import { Injectable } from '@angular/core';
import SockJS from 'sockjs-client';
import * as Stomp from 'stompjs';

const MAX_RETRIES = 5;

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {

  private stompClient: any;
  private retryCount = 0;
  private pendingSubscriptions: Array<{ topic: string; cb: (message: any) => void }> = [];
  private activeSubscriptions = new Set<string>();

  /** Backward compatible: support topic */
  connect(callback: (message: any) => void) {
    this.subscribe('/topic/support', callback);
  }

  subscribe(topic: string, callback: (message: any) => void) {
    if (this.activeSubscriptions.has(topic)) return;
    this.activeSubscriptions.add(topic);

    if (this.stompClient && this.stompClient.connected) {
      this.subscribeInternal(topic, callback);
      return;
    }

    this.pendingSubscriptions.push({ topic, cb: callback });
    this.ensureConnected();
  }

  subscribeNotifications(userId: number, callback: (message: any) => void) {
    this.subscribe(`/topic/notifications/${userId}`, callback);
  }

  /** Subscribe to admin real-time events. Callback receives { type: string }. */
  subscribeAdmin(callback: (event: { type: string }) => void) {
    this.subscribe('/topic/admin', callback);
  }

  private ensureConnected() {
    if (this.stompClient && this.stompClient.connected) return;

    const socket = new SockJS('http://localhost:8080/ws');
    this.stompClient = Stomp.over(socket);

    // Suppress verbose STOMP frame logs
    this.stompClient.debug = () => { };

    this.stompClient.connect({}, (frame: any) => {
      this.retryCount = 0; // reset on successful connection

      const items = [...this.pendingSubscriptions];
      this.pendingSubscriptions = [];
      items.forEach(({ topic, cb }) => this.subscribeInternal(topic, cb));
    }, (error: any) => {
      console.error('WebSocket connection error:', error);
      if (this.retryCount < MAX_RETRIES) {
        this.retryCount++;
        const delay = Math.min(5000 * this.retryCount, 30000);
        setTimeout(() => this.ensureConnected(), delay);
      } else {
        console.warn('WebSocket max retries reached. Giving up.');
      }
    });
  }

  private subscribeInternal(topic: string, callback: (message: any) => void) {
    this.stompClient.subscribe(topic, (msg: any) => {
      try {
        callback(JSON.parse(msg.body));
      } catch (e) {
        console.error('Failed to parse WebSocket message:', e);
      }
    });
  }

  disconnect() {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.disconnect();
    }
    this.retryCount = 0;
    this.pendingSubscriptions = [];
    this.activeSubscriptions.clear();
  }
}