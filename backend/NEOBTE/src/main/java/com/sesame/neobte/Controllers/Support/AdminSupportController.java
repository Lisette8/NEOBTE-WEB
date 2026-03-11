package com.sesame.neobte.Controllers.Support;

import com.sesame.neobte.DTO.Responses.Support.SupportResponseDTO;
import com.sesame.neobte.Entities.Class.Support;
import com.sesame.neobte.Services.SupportService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.*;

@AllArgsConstructor
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/support")
public class AdminSupportController {

    private SupportService supportService;


    @GetMapping("/all")
    public List<SupportResponseDTO> getAllTickets() {
        return supportService.getAllTickets();
    }

    @PutMapping("/update/{id}")
    public Support update(
            @PathVariable Long id,
            @RequestParam String response,
            @RequestParam String status
    ) {
        return supportService.updateStatus(id, response, status);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        supportService.deleteTicket(id);
    }
}
