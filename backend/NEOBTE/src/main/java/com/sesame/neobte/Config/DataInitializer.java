package com.sesame.neobte.Config;

import com.sesame.neobte.Entities.Class.CompteInterne;
import com.sesame.neobte.Entities.Class.Fraude.FraudeConfig;
import com.sesame.neobte.Entities.Class.Investment.InvestmentPlan;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.Genre;
import com.sesame.neobte.Entities.Enumeration.Role;
import com.sesame.neobte.Repositories.Fraude.IFraudeConfigRepository;
import com.sesame.neobte.Repositories.ICompteInterneRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Repositories.Investment.IInvestmentPlanRepository;
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
    private IFraudeConfigRepository fraudeConfigRepository;
    private PasswordEncoder passwordEncoder;
    private IInvestmentPlanRepository investmentPlanRepository;

    @Override
    public void run(String... args) throws Exception {

        // Seed admin user
        if (utilisateurRepository.findByEmail("admin@gmail.com") == null) {
            Utilisateur admin = new Utilisateur();
            admin.setEmail("admin@gmail.com");
            admin.setUsername("admin");
            admin.setMotDePasse(passwordEncoder.encode("admin123"));
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

        // Seed investment pool accounts
        if (compteInterneRepository.findByNom("NEOBTE_INVESTMENTS").isEmpty()) {
            CompteInterne inv = new CompteInterne();
            inv.setNom("NEOBTE_INVESTMENTS");
            inv.setSolde(0.0);
            compteInterneRepository.save(inv);
            System.out.println(">>> Investment pool (NEOBTE_INVESTMENTS) initialized");
        }
        if (compteInterneRepository.findByNom("NEOBTE_RESERVES").isEmpty()) {
            CompteInterne res = new CompteInterne();
            res.setNom("NEOBTE_RESERVES");
            res.setSolde(0.0);
            compteInterneRepository.save(res);
            System.out.println(">>> Reserve account (NEOBTE_RESERVES) initialized");
        }
        if (compteInterneRepository.findByNom("NEOBTE_DEPLOYED").isEmpty()) {
            CompteInterne dep = new CompteInterne();
            dep.setNom("NEOBTE_DEPLOYED");
            dep.setSolde(0.0);
            compteInterneRepository.save(dep);
            System.out.println(">>> Deployed capital account (NEOBTE_DEPLOYED) initialized");
        }

        // Seed default fraud config
        if (fraudeConfigRepository.findById(1L).isEmpty()) {
            FraudeConfig cfg = new FraudeConfig();
            cfg.setId(1L);
            fraudeConfigRepository.save(cfg);
            System.out.println(">>> Default fraud config initialized");
        }
        // Seed default investment plans
        if (investmentPlanRepository.count() == 0) {
            investmentPlanRepository.save(plan("Court terme — 3 mois",
                    "Idéal pour placer votre épargne sur une courte durée avec un rendement sûr.", 3, 0.035, 500, 20000));
            investmentPlanRepository.save(plan("Moyen terme — 6 mois",
                    "Bon équilibre entre liquidité et rendement pour vos projets à mi-chemin.", 6, 0.055, 1000, 50000));
            investmentPlanRepository.save(plan("Long terme — 12 mois",
                    "Maximisez votre rendement en immobilisant votre capital sur un an.", 12, 0.075, 2000, 100000));
            investmentPlanRepository.save(plan("Premium — 24 mois",
                    "Le meilleur taux pour les investisseurs patients. Capital protégé, rendement élevé.", 24, 0.095, 5000, 500000));
            System.out.println(">>> Default investment plans initialized");
        }
    }

    private InvestmentPlan plan(String nom, String desc, int mois, double taux, double min, double max) {
        InvestmentPlan p = new InvestmentPlan();
        p.setNom(nom); p.setDescription(desc); p.setDureeEnMois(mois);
        p.setTauxAnnuel(taux); p.setMontantMin(min); p.setMontantMax(max); p.setActif(true);
        return p;
    }
}