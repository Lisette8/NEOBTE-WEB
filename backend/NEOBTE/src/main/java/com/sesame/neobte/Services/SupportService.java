package com.sesame.neobte.Services;

import com.sesame.neobte.Entities.Support;

import java.util.*;

public interface SupportService {


    //client side methods
    Support createTicket(Long userId, String sujet, String message);
    List<Support> getMyTickets(Long userId);

    //admin side methods
    List<Support> getAllTickets();
    Support updateStatus(Long id, String response, String status);
    void deleteTicket(Long id);
}
