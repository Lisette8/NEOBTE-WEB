package com.sesame.neobte.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


public class EmailTemplateLoader {

    public static String loadTemplate(String filename, String[][] replacements) {
        try {
            InputStream is = EmailTemplateLoader.class.getClassLoader()
                    .getResourceAsStream("templates/" + filename);
            if (is == null) throw new RuntimeException("Template " + filename + " not found");
            String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            for (String[] pair : replacements) {
                template = template.replace("{{" + pair[0] + "}}", pair[1]);
            }
            return template;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load template: " + filename, e);
        }
    }

    public static String loadSupportReplyTemplate(String subject, String reply) {
        return loadTemplate("support-reply.html", new String[][]{
                {"subject", subject}, {"reply", reply}
        });
    }

    public static String loadDemandeConfirmationTemplate(String prenom, String typeCompte) {
        return loadTemplate("demande-confirmation.html", new String[][]{
                {"prenom", prenom}, {"typeCompte", formatTypeCompte(typeCompte)}
        });
    }

    public static String loadDemandeApprovalTemplate(String prenom, String typeCompte, Long compteId) {
        return loadTemplate("demande-acceptee.html", new String[][]{
                {"prenom", prenom}, {"typeCompte", formatTypeCompte(typeCompte)},
                {"compteId", String.valueOf(compteId)}
        });
    }

    public static String loadDemandeRejectionTemplate(String prenom, String typeCompte, String reason) {
        return loadTemplate("demande-refusee.html", new String[][]{
                {"prenom", prenom}, {"typeCompte", formatTypeCompte(typeCompte)}, {"reason", reason}
        });
    }

    public static String loadPasswordResetTemplate(String prenom, String code) {
        return loadTemplate("password-reset.html", new String[][]{
                {"prenom", prenom}, {"code", code}
        });
    }

    public static String loadFraudeAlertTemplate(String adminPrenom, String clientName,
                                                 String clientEmail, String alertType,
                                                 String severity, String description) {
        return loadTemplate("fraude-alert.html", new String[][]{
                {"adminPrenom", adminPrenom},
                {"clientName", clientName},
                {"clientEmail", clientEmail},
                {"alertType", alertType},
                {"severity", severity},
                {"description", description}
        });
    }

    private static String formatTypeCompte(String typeCompte) {
        if (typeCompte == null) return "";
        return switch (typeCompte) {
            case "COURANT"       -> "Compte Chèque";
            case "EPARGNE"       -> "Compte Épargne";
            case "PROFESSIONNEL" -> "Compte Professionnel";
            default              -> typeCompte;
        };
    }
}