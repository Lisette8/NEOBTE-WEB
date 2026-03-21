package com.sesame.neobte.Controllers;


import com.sesame.neobte.DTO.Requests.Admin.CreateUserRequest;
import com.sesame.neobte.DTO.Requests.Admin.UpdateUserRequest;
import com.sesame.neobte.DTO.Responses.Admin.AdminUserResponse;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.Role;
import com.sesame.neobte.Repositories.ICompteRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Repositories.IVirementRepository;
import com.sesame.neobte.Services.AdministrateurService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin")
public class AdministrateurController {

    private final AdministrateurService administrateurService;
    private final IUtilisateurRepository utilisateurRepository;
    private final IVirementRepository virementRepository;
    private final ICompteRepository compteRepository;



    @GetMapping("/all")
    public List<AdminUserResponse> getAllUsers() {
        return administrateurService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public AdminUserResponse getUserById(@PathVariable Long id) {
        return administrateurService.getUserById(id);
    }

    @PostMapping("/users")
    public Utilisateur createUser(@RequestBody CreateUserRequest DTO) {
        return administrateurService.createUtilisateur(DTO);
    }

    @PutMapping("/users/{id}")
    public Utilisateur updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest dto
    ) {
        return administrateurService.updateUser(id, dto);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        administrateurService.deleteUser(id);
    }

    /** Toggle premium subscription for a client user. */
    @PutMapping("/users/{id}/premium")
    public Map<String, Object> setPremium(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean premium = Boolean.TRUE.equals(body.get("premium"));
        administrateurService.setPremium(id, premium);
        return Map.of("userId", id, "premium", premium);
    }


    /** Dashboard stats — counts and totals for the home landing page */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        long totalUsers     = utilisateurRepository.countByRole(Role.CLIENT);
        long totalAdmins    = utilisateurRepository.countByRole(Role.ADMIN);
        long totalTransfers = virementRepository.countTotal();
        Double totalVolume  = virementRepository.totalVolume();
        Double avgTransfer  = virementRepository.avgTransfer();
        long totalAccounts  = compteRepository.count();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalClients",   totalUsers);
        stats.put("totalAdmins",    totalAdmins);
        stats.put("totalTransfers", totalTransfers);
        stats.put("totalVolume",    totalVolume != null ? totalVolume : 0.0);
        stats.put("avgTransfer",    avgTransfer != null ? avgTransfer : 0.0);
        stats.put("totalAccounts",  totalAccounts);
        return stats;
    }

}
