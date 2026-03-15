package com.sesame.neobte.Config;

import com.sesame.neobte.Entities.Class.CompteInterne;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.Genre;
import com.sesame.neobte.Entities.Enumeration.Role;
import com.sesame.neobte.Repositories.ICompteInterneRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;



//Data Initializer for ADMIN
@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private IUtilisateurRepository utilisateurRepository;
    private ICompteInterneRepository compteInterneRepository;
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // Seed admin user
        if (utilisateurRepository.findByEmail("admin@gmail.com") == null) {
            Utilisateur admin = new Utilisateur();
            admin.setEmail("admin@gmail.com");
            admin.setUsername("admin");
            admin.setMotDePasse(passwordEncoder.encode("Admin123"));
            admin.setNom("Admin");
            admin.setPrenom("NEO BTE");
            admin.setCin("00000000");
            admin.setTelephone("+21671112000");
            admin.setDateNaissance(LocalDate.of(1990, 1, 1));
            admin.setJob("System Administrator");
            admin.setAdresse("Boulevard Beji Caid Essebsi, Centre Urbain Nord");
            admin.setCodePostal("1082");
            admin.setPays("Tunisie");
            admin.setGenre(Genre.HOMME);
            admin.setDateCreationCompte(new Date());
            admin.setRole(Role.ADMIN);
            utilisateurRepository.save(admin);
            System.out.println(">>> Default admin account initialized");
        }

        // Seed internal fee account — created once, never recreated
        if (compteInterneRepository.findByNom("NEOBTE_FEES").isEmpty()) {
            CompteInterne fees = new CompteInterne();
            fees.setNom("NEOBTE_FEES");
            fees.setSolde(0.0);
            compteInterneRepository.save(fees);
            System.out.println(">>> Internal fee account (NEOBTE_FEES) initialized");
        }
    }
}