import { Injectable } from '@angular/core';
import SockJS from 'sockjs-client';
import * as Stomp from 'stompjs';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {

  private stompClient: any;

  connect(callback: (message: any) => void) {
    console.log('Attempting to connect to WebSocket at http://localhost:8080/ws...');
    const socket = new SockJS('http://localhost:8080/ws');
    this.stompClient = Stomp.over(socket);

    // Disable logging in console unless needed for debugging
    // this.stompClient.debug = () => {};

    this.stompClient.connect({}, (frame: any) => {
      console.log('Connected to WebSocket successfully: ' + frame);
      this.stompClient.subscribe('/topic/support', (msg: any) => {
        console.log('Received message from WebSocket /topic/support:', msg.body);
        callback(JSON.parse(msg.body));
      });
    }, (error: any) => {
      console.error('WebSocket connection error:', error);
      // Try to reconnect after 5 seconds
      setTimeout(() => this.connect(callback), 5000);
    });
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.disconnect();
    }
  }
}