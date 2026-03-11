package com.sesame.neobte.Config;

import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.Role;
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

    private IUtilisateurRepository utilisateurRepository;
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // if admin already exists
        Utilisateur existingAdmin = utilisateurRepository.findByEmail("admin@site.com");

        if (existingAdmin == null) {
            Utilisateur admin = new Utilisateur();

            admin.setEmail("admin@gmail.com");
            admin.setMotDePasse(passwordEncoder.encode("admin123"));
            admin.setNom("Admin");
            admin.setPrenom("Super");
            admin.setAdresse("Tunis");
            admin.setAge(30);
            admin.setJob("Manager");
            admin.setGenre(null);
            admin.setDateCreationCompte(new Date());
            admin.setRole(Role.ADMIN);

            utilisateurRepository.save(admin);

            System.out.println("ADMIN CREATED SUCCESSFULLY");
        }
    }
}
