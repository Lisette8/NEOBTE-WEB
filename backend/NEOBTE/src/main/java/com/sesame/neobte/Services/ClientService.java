package com.sesame.neobte.Services;

import com.sesame.neobte.Entities.Client;
import java.util.*;

public interface ClientService {

    Client createClient(Client client);
    List<Client> getAllClients();
    Client getClientById(Long id);
    Client updateClient(Long id, Client client);
    void deleteClient(Long id);
}
