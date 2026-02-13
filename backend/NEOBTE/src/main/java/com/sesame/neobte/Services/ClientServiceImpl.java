package com.sesame.neobte.Services;

import com.sesame.neobte.Entities.Client;
import com.sesame.neobte.Repositories.IClientRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ClientServiceImpl implements ClientService {

    @Autowired
    private IClientRepository clientRepository;
    private PasswordEncoder passwordEncoder;

    //methods
    @Override
    public Client createClient(Client client) {
        client.setMotDePasse(passwordEncoder.encode(client.getMotDePasse()));
        return clientRepository.save(client);
    }

    @Override
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Override
    public Client getClientById(Long id) {
        return clientRepository.findById(id).get();
    }

    @Override
    public Client updateClient(Long id, Client newClient) {
        Client oldClient = clientRepository.findById(id).get();

        oldClient.setNom(newClient.getNom());
        oldClient.setPrenom(newClient.getPrenom());
        oldClient.setAge(newClient.getAge());
        oldClient.setAdresse(newClient.getAdresse());
        oldClient.setJob(newClient.getJob());
        oldClient.setGenre(newClient.getGenre());
        oldClient.setSolde(newClient.getSolde());

        return clientRepository.save(oldClient);
    }

    @Override
    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }
}
