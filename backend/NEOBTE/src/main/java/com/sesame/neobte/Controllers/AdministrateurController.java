package com.sesame.neobte.Controllers;


import com.sesame.neobte.Services.AdministrateurService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/admin")
public class AdministrateurController {

    private final AdministrateurService administrateurService;

    @GetMapping("/hello")
        public String hello(){
            return "Hello World";
        }
}
