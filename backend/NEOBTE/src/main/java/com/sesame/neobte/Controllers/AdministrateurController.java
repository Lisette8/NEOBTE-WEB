package com.sesame.neobte.Controllers;


import com.sesame.neobte.DTO.Requests.Admin.CreateUserRequest;
import com.sesame.neobte.DTO.Requests.Admin.UpdateUserRequest;
import com.sesame.neobte.DTO.Responses.Admin.AdminUserResponse;
import com.sesame.neobte.Entities.Utilisateur;
import com.sesame.neobte.Services.AdministrateurService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin")
public class AdministrateurController {

    private final AdministrateurService administrateurService;



    @GetMapping("/all")
    public List<Utilisateur> getAllUsers() {
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


}
