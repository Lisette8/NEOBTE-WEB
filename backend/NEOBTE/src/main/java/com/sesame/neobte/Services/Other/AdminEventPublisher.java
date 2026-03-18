package com.sesame.neobte.Services.Other;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

/**
 * Central publisher for admin real-time events.
 * Every mutation in the system calls publish(type) and all admin
 * components receive a lightweight {type} payload over WebSocket,
 * then reload the relevant data from the REST API.
 *
 * Topic: /topic/admin
 * Payload: { "type": "VIREMENT" | "USER" | "COMPTE" | "DEMANDE" | "FRAUDE" | "ACTUALITE" | "SUPPORT" }
 */
@Component
@RequiredArgsConstructor
public class AdminEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public enum EventType {
        VIREMENT, USER, COMPTE, DEMANDE, FRAUDE, ACTUALITE, SUPPORT
    }

    /**
     * Publish after current transaction commits (if inside one),
     * or immediately if called outside a transaction.
     */
    public void publish(EventType type) {
        Map<String, String> payload = Map.of("type", type.name());

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messagingTemplate.convertAndSend("/topic/admin", payload);
                }
            });
        } else {
            messagingTemplate.convertAndSend("/topic/admin", payload);
        }
    }
}