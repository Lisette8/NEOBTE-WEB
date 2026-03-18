package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Actualite.ActualiteCreateDTO;
import com.sesame.neobte.DTO.Responses.Actualite.ActualiteResponseDTO;
import com.sesame.neobte.Entities.Class.Actualite;
import com.sesame.neobte.Entities.Class.ActualiteReaction;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;
import com.sesame.neobte.Repositories.IActualiteReactionRepository;
import com.sesame.neobte.Repositories.IActualiteRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Entities.Enumeration.NotificationType;
import com.sesame.neobte.Services.Other.AdminEventPublisher;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@AllArgsConstructor
@Service
public class ActualiteServiceImpl implements ActualiteService {

    private final IActualiteRepository actualiteRepository;
    private final IUtilisateurRepository utilisateurRepository;
    private final IActualiteReactionRepository reactionRepository;
    private final NotificationService notificationService;
    private final AdminEventPublisher adminEventPublisher;

    @Override
    public Page<ActualiteResponseDTO> getAll(int page, int size, Long callerUserId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Actualite> actualites = actualiteRepository.findAllByOrderByDateCreationActualiteDesc(pageable);

        List<Long> ids = actualites.getContent().stream().map(Actualite::getIdActualite).toList();
        ReactionBundle bundle = loadReactions(ids, callerUserId);

        return actualites.map(a -> mapToResponseDTO(
                a,
                bundle.countsByPost.getOrDefault(a.getIdActualite(), Map.of()),
                bundle.myReactionByPost.get(a.getIdActualite())
        ));
    }

    @Override
    public ActualiteResponseDTO getById(Long id, Long callerUserId) {
        Actualite actualite = actualiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actualité introuvable"));

        ReactionBundle bundle = loadReactions(List.of(id), callerUserId);
        return mapToResponseDTO(
                actualite,
                bundle.countsByPost.getOrDefault(id, Map.of()),
                bundle.myReactionByPost.get(id)
        );
    }

    @Override
    public ActualiteResponseDTO createActualite(Long adminId, ActualiteCreateDTO dto, String imageUrl) {
        Utilisateur admin = utilisateurRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin introuvable"));

        Actualite actualite = new Actualite();
        actualite.setTitre(requireNonBlank(dto.getTitre(), "Le titre est obligatoire"));
        actualite.setSousTitre(trimToNull(dto.getSousTitre()));
        actualite.setDescription(trimToNull(dto.getDescription()));
        actualite.setContenu(trimToNull(dto.getContenu()));
        actualite.setCategorie(trimToNull(dto.getCategorie()));
        actualite.setImageUrl(trimToNull(imageUrl));
        actualite.setCreateur(admin);

        Actualite saved = actualiteRepository.save(actualite);
        notificationService.notifyAllClients(
                NotificationType.ACTUALITE_CREATED,
                "Nouvelle actualité",
                "Une nouvelle actualité a été publiée : " + saved.getTitre(),
                "/actualite-view"
        );
        ActualiteResponseDTO r = mapToResponseDTO(saved, Map.of(), null);
        adminEventPublisher.publish(AdminEventPublisher.EventType.ACTUALITE);
        return r;
    }

    @Override
    public ActualiteResponseDTO updateActualite(Long id, ActualiteCreateDTO dto, String imageUrl) {
        Actualite actualiteUpdated = actualiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actualité introuvable"));

        actualiteUpdated.setTitre(requireNonBlank(dto.getTitre(), "Le titre est obligatoire"));
        actualiteUpdated.setSousTitre(trimToNull(dto.getSousTitre()));
        actualiteUpdated.setDescription(trimToNull(dto.getDescription()));
        actualiteUpdated.setContenu(trimToNull(dto.getContenu()));
        actualiteUpdated.setCategorie(trimToNull(dto.getCategorie()));
        if (imageUrl != null) actualiteUpdated.setImageUrl(trimToNull(imageUrl));

        Actualite saved = actualiteRepository.save(actualiteUpdated);
        notificationService.notifyAllClients(
                NotificationType.ACTUALITE_UPDATED,
                "Actualité mise à jour",
                "Une actualité a été mise à jour : " + saved.getTitre(),
                "/actualite-view"
        );

        ReactionBundle bundle = loadReactions(List.of(id), null);
        ActualiteResponseDTO r2 = mapToResponseDTO(saved, bundle.countsByPost.getOrDefault(id, Map.of()), null);
        adminEventPublisher.publish(AdminEventPublisher.EventType.ACTUALITE);
        return r2;
    }

    @Override
    public void deleteActualite(Long id) {
        reactionRepository.deleteByActualite_IdActualite(id);
        actualiteRepository.deleteById(id);
        adminEventPublisher.publish(AdminEventPublisher.EventType.ACTUALITE);
    }

    @Override
    public ActualiteResponseDTO react(Long actualiteId, Long userId, String reaction) {
        String raw = requireNonBlank(reaction, "Réaction invalide");
        String reactionCode = normalizeReaction(raw);
        if (!ALLOWED_REACTIONS.contains(reactionCode)) throw new BadRequestException("Réaction non supportée.");

        Actualite post = actualiteRepository.findById(actualiteId)
                .orElseThrow(() -> new ResourceNotFoundException("Actualité introuvable"));
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        Optional<ActualiteReaction> existing = reactionRepository
                .findByActualite_IdActualiteAndUtilisateur_IdUtilisateur(actualiteId, userId);

        String myReaction;
        if (existing.isPresent()) {
            ActualiteReaction existingReaction = existing.get();
            String current = normalizeReaction(existingReaction.getEmoji());
            if (reactionCode.equals(current)) {
                reactionRepository.delete(existingReaction);
                myReaction = null;
            } else {
                existingReaction.setEmoji(reactionCode);
                reactionRepository.save(existingReaction);
                myReaction = reactionCode;
            }
        } else {
            ActualiteReaction newReaction = new ActualiteReaction();
            newReaction.setActualite(post);
            newReaction.setUtilisateur(user);
            newReaction.setEmoji(reactionCode);
            reactionRepository.save(newReaction);
            myReaction = reactionCode;
        }

        ReactionBundle bundle = loadReactions(List.of(actualiteId), userId);
        return mapToResponseDTO(
                post,
                bundle.countsByPost.getOrDefault(actualiteId, Map.of()),
                myReaction
        );
    }

    private ActualiteResponseDTO mapToResponseDTO(Actualite a, Map<String, Long> reactions, String myReaction) {
        return ActualiteResponseDTO.builder()
                .idActualite(a.getIdActualite())
                .titre(a.getTitre())
                .sousTitre(a.getSousTitre())
                .description(a.getDescription())
                .contenu(a.getContenu())
                .categorie(a.getCategorie())
                .imageUrl(a.getImageUrl())
                .dateCreationActualite(a.getDateCreationActualite())
                .dateMajActualite(a.getDateMajActualite())
                .createurId(a.getCreateur() != null ? a.getCreateur().getIdUtilisateur() : null)
                .reactions(reactions)
                .myReaction(myReaction)
                .build();
    }

    private record ReactionBundle(
            Map<Long, Map<String, Long>> countsByPost,
            Map<Long, String> myReactionByPost
    ) {}

    private ReactionBundle loadReactions(List<Long> postIds, Long callerUserId) {
        if (postIds == null || postIds.isEmpty()) return new ReactionBundle(Map.of(), Map.of());

        Map<Long, Map<String, Long>> counts = new HashMap<>();
        for (Object[] row : reactionRepository.countByActualiteIdsGrouped(postIds)) {
            Long postId = (Long) row[0];
            String emoji = (String) row[1];
            String reaction = normalizeReaction(emoji);
            Long cnt = (Long) row[2];
            counts.computeIfAbsent(postId, _id -> new LinkedHashMap<>()).merge(reaction, cnt, Long::sum);
        }

        Map<Long, String> my = new HashMap<>();
        if (callerUserId != null) {
            reactionRepository.findUserReactionsForPosts(callerUserId, postIds)
                    .forEach(r -> my.put(r.getActualite().getIdActualite(), normalizeReaction(r.getEmoji())));
        }

        return new ReactionBundle(counts, my);
    }

    private static final Set<String> ALLOWED_REACTIONS = Set.of(
            "LIKE", "CELEBRATE", "SUPPORT", "LOVE", "INSIGHTFUL", "CURIOUS", "FUNNY"
    );

    private static final Map<String, String> LEGACY_EMOJI_TO_REACTION = Map.of(
            "👍", "LIKE",
            "❤️", "LOVE",
            "😂", "FUNNY",
            "😮", "CURIOUS",
            "😢", "SUPPORT",
            "😡", "CURIOUS"
    );

    private static String normalizeReaction(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return null;
        String mapped = LEGACY_EMOJI_TO_REACTION.get(trimmed);
        if (mapped != null) return mapped;
        return trimmed.toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String requireNonBlank(String s, String message) {
        String t = trimToNull(s);
        if (t == null) throw new BadRequestException(message);
        return t;
    }
}
