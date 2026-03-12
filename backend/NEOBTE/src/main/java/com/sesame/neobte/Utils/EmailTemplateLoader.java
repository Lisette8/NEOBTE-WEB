package com.sesame.neobte.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EmailTemplateLoader {


    //support
    public static String loadSupportReplyTemplate(String subject, String reply) {

        try {
            InputStream inputStream = EmailTemplateLoader.class
                    .getClassLoader()
                    .getResourceAsStream("templates/support-reply.html");

            if (inputStream == null) {
                throw new RuntimeException("Email template not found");
            }

            String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            template = template.replace("{{subject}}", subject);
            template = template.replace("{{reply}}", reply);

            return template;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load email template", e);
        }



        //demande compte
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
                        .getResourceAsStream("templates/demande-approval.html");
                if (is == null) throw new RuntimeException("Template demande-approval.html not found");
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
                        .getResourceAsStream("templates/demande-rejection.html");
                if (is == null) throw new RuntimeException("Template demande-rejection.html not found");
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
            return switch (typeCompte) {
                case "COURANT"        -> "Compte Chèque";
                case "EPARGNE"        -> "Compte Épargne";
                case "PROFESSIONNEL"  -> "Compte Professionnel";
                default               -> typeCompte;
            };
        }
    }
}