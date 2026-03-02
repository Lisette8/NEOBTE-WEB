package com.sesame.neobte.Controllers.Support;

import com.sesame.neobte.Entities.Support;
import com.sesame.neobte.Services.SupportService;
import org.springframework.web.bind.annotation.*;


import java.util.*;

@RestController
@RequestMapping("/api/admin/support")
public class AdminSupportController {

    private SupportService supportService;


    @GetMapping("/all")
    public List<Support> getAll() {
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
