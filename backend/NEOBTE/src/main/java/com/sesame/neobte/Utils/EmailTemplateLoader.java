package com.sesame.neobte.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EmailTemplateLoader {

    public static String loadSupportReplyTemplate(String subject, String reply) {
        try {
            InputStream is = EmailTemplateLoader.class.getClassLoader()
                    .getResourceAsStream("templates/support-reply.html");
            if (is == null) throw new RuntimeException("Template support-reply.html not found");
            String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            template = template.replace("{{subject}}", subject);
            template = template.replace("{{reply}}", reply);
            return template;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load support reply template", e);
        }
    }

    public static String loadDemandeConfirmationTemplate(String prenom, String typeCompte) {
        try {
            InputStream is = EmailTemplateLoader.class.getClassLoader()
                    .getResourceAsStream("templates/demande-confirmation.html");
            if (is == null) throw new RuntimeException("Template demande-confirmation.html not found");
            String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            template = template.replace("{{prenom}}", prenom);
            template = template.replace("{{typeCompte}}", formatTypeCompte(typeCompte));
            return template;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load demande confirmation template", e);
        }
    }

    public static String loadDemandeApprovalTemplate(String prenom, String typeCompte, Long compteId) {
        try {
            InputStream is = EmailTemplateLoader.class.getClassLoader()
                    .getResourceAsStream("templates/demande-acceptee.html");
            if (is == null) throw new RuntimeException("Template demande-acceptee.html not found");
            String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            template = template.replace("{{prenom}}", prenom);
            template = template.replace("{{typeCompte}}", formatTypeCompte(typeCompte));
            template = template.replace("{{compteId}}", String.valueOf(compteId));
            return template;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load demande approval template", e);
        }
    }

    public static String loadDemandeRejectionTemplate(String prenom, String typeCompte, String reason) {
        try {
            InputStream is = EmailTemplateLoader.class.getClassLoader()
                    .getResourceAsStream("templates/demande-refusee.html");
            if (is == null) throw new RuntimeException("Template demande-refusee.html not found");
            String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            template = template.replace("{{prenom}}", prenom);
            template = template.replace("{{typeCompte}}", formatTypeCompte(typeCompte));
            template = template.replace("{{reason}}", reason);
            return template;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load demande rejection template", e);
        }
    }

    private static String formatTypeCompte(String typeCompte) {
        if (typeCompte == null) return "";
        if (typeCompte.equals("COURANT"))       return "Compte Chèque";
        if (typeCompte.equals("EPARGNE"))       return "Compte Épargne";
        if (typeCompte.equals("PROFESSIONNEL")) return "Compte Professionnel";
        return typeCompte;
    }
}