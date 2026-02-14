package com.sesame.neobte.Controllers;

import com.sesame.neobte.Entities.Client;
import com.sesame.neobte.Services.ClientService;
import lombok.AllArgsConstructor;
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

    @PostMapping("/createClient")
    public Client createClient(@RequestBody Client client) {
        return clientService.createClient(client);
    }
}
