package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Support.SupportCreateDTO;
import com.sesame.neobte.DTO.Responses.Support.SupportResponseDTO;
import com.sesame.neobte.Entities.Support;

import java.util.*;

public interface SupportService {


    //client side methods
    SupportResponseDTO createTicket(Long userId, SupportCreateDTO dto);
    List<SupportResponseDTO> getMyTickets(Long userId);

    //admin side methods
    List<SupportResponseDTO> getAllTickets();
    Support updateStatus(Long id, String response, String status);
    void deleteTicket(Long id);
}
