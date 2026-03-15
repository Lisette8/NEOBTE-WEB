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

  connect(callback: (message: any) => void) {
    const socket = new SockJS('http://localhost:8080/ws');
    this.stompClient = Stomp.over(socket);

    // Suppress verbose STOMP frame logs
    this.stompClient.debug = () => {};

    this.stompClient.connect({}, (frame: any) => {
      this.retryCount = 0; // reset on successful connection
      this.stompClient.subscribe('/topic/support', (msg: any) => {
        try {
          callback(JSON.parse(msg.body));
        } catch (e) {
          console.error('Failed to parse WebSocket message:', e);
        }
      });
    }, (error: any) => {
      console.error('WebSocket connection error:', error);
      if (this.retryCount < MAX_RETRIES) {
        this.retryCount++;
        const delay = Math.min(5000 * this.retryCount, 30000); // exponential-ish backoff, max 30s
        setTimeout(() => this.connect(callback), delay);
      } else {
        console.warn('WebSocket max retries reached. Giving up.');
      }
    });
  }

  disconnect() {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.disconnect();
    }
    this.retryCount = 0;
  }
}