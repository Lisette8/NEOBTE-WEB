package com.sesame.neobte.Controllers.Actualite;

import com.sesame.neobte.DTO.Requests.Actualite.ActualiteCreateDTO;
import com.sesame.neobte.DTO.Responses.Actualite.ActualiteResponseDTO;
import com.sesame.neobte.Services.ActualiteService;
import com.sesame.neobte.Services.Other.MediaStorageService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/actualite")
@PreAuthorize("hasRole('ADMIN')")
@AllArgsConstructor
public class AdminActualiteController {

    private ActualiteService actualiteService;
    private MediaStorageService mediaStorageService;


    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ActualiteResponseDTO create(
            @RequestBody ActualiteCreateDTO dto,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();

        return actualiteService.createActualite(adminId, dto, null);
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActualiteResponseDTO createWithImage(
            @RequestPart("data") ActualiteCreateDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        String imageUrl = mediaStorageService.storeActualiteImage(image);
        return actualiteService.createActualite(adminId, dto, imageUrl);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ActualiteResponseDTO update(
            @PathVariable Long id,
            @RequestBody ActualiteCreateDTO dto
    ) {
        return actualiteService.updateActualite(id, dto, null);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActualiteResponseDTO updateWithImage(
            @PathVariable Long id,
            @RequestPart("data") ActualiteCreateDTO dto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        String imageUrl = mediaStorageService.storeActualiteImage(image);
        return actualiteService.updateActualite(id, dto, imageUrl);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(
            @PathVariable Long id
    ) {
        actualiteService.deleteActualite(id);
    }

}
