package com.sesame.neobte.Controllers.Support;

import com.sesame.neobte.Entities.Support;
import com.sesame.neobte.Services.SupportService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/client/support")
@AllArgsConstructor
public class ClientSupportController {

    private SupportService supportService;

    @PostMapping("/add")
    public Support create(
            @RequestParam String sujet,
            @RequestParam String message,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return supportService.createTicket(userId, sujet, message);
    }

    @GetMapping("/myTickets")
    public List<Support> myTickets(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return supportService.getMyTickets(userId);
    }

}
