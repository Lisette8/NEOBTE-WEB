package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Contact.ContactCreateDTO;
import com.sesame.neobte.DTO.Requests.Support.SupportCreateDTO;
import com.sesame.neobte.DTO.Responses.Support.SupportResponseDTO;
import com.sesame.neobte.Entities.Class.Support;

import java.util.*;

public interface SupportService {

    // client (authenticated) methods
    SupportResponseDTO createTicket(Long userId, SupportCreateDTO dto);
    List<SupportResponseDTO> getMyTickets(Long userId);

    // public contact form — no auth required
    SupportResponseDTO createGuestTicket(ContactCreateDTO dto);

    // admin methods
    List<SupportResponseDTO> getAllTickets();
    SupportResponseDTO updateStatus(Long id, String response, String status);
    void deleteTicket(Long id);
}