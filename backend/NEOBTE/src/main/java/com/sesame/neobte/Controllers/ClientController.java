package com.sesame.neobte.Controllers;

import com.sesame.neobte.Entities.Client;
import com.sesame.neobte.Services.ClientService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/client")
public class ClientController {

    ClientService clientService;

    @GetMapping("/all")
    public List<Client> getAllClients() {
        return clientService.getAllClients();
    }


    @GetMapping("/current")
    public Client getMyProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return clientService.getClientById(userId);
    }


    @PutMapping("/current")
    public Client updateProfile(@RequestBody Client newClient,
                                Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        return clientService.updateClient(userId, newClient);
    }

    @DeleteMapping("/current")
    public void deleteMyAccount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        clientService.deleteClient(userId);
    }


}