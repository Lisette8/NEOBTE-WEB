package com.sesame.neobte.Config;

import com.sesame.neobte.Entities.Utilisateur;
import com.sesame.neobte.Entities.Role;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;



//Data Initializer for ADMIN
@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private IUtilisateurRepository clientRepository;
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // if admin already exists
        Utilisateur existingAdmin = clientRepository.findByEmail("admin@site.com");

        if (existingAdmin == null) {
            Utilisateur admin = new Utilisateur();

            admin.setEmail("admin@site.com");
            admin.setMotDePasse(passwordEncoder.encode("admin123"));
            admin.setNom("Admin");
            admin.setPrenom("Super");
            admin.setAdresse("Tunis");
            admin.setAge(30);
            admin.setJob("Manager");
            admin.setGenre(null);
            admin.setSolde(0.0);
            admin.setDateCreationCompte(new Date());
            admin.setRole(Role.valueOf("ADMIN"));

            clientRepository.save(admin);

            System.out.println("ADMIN CREATED SUCCESSFULLY");
        }
    }
}
