package com.sesame.neobte.Services.Other;

import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import com.sesame.neobte.Utils.EmailTemplateLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService {

    private JavaMailSender mailSender;

    private void send(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("neobte-reply@gmail.com");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }

    public void sendSupportResponseEmail(String to, String subject, String adminReply) {
        send(to, "Support reply: " + subject,
                EmailTemplateLoader.loadSupportReplyTemplate(subject, adminReply));
    }

    public void sendDemandeConfirmationEmail(String to, String prenom, String typeCompte) {
        send(to, "NEO BTE — Your account request has been received",
                EmailTemplateLoader.loadDemandeConfirmationTemplate(prenom, typeCompte));
    }

    public void sendDemandeApprovalEmail(String to, String prenom, String typeCompte, Long compteId) {
        send(to, "NEO BTE — Your account has been approved!",
                EmailTemplateLoader.loadDemandeApprovalTemplate(prenom, typeCompte, compteId));
    }

    public void sendDemandeRejectionEmail(String to, String prenom, String typeCompte, String reason) {
        send(to, "NEO BTE — Update on your account request",
                EmailTemplateLoader.loadDemandeRejectionTemplate(prenom, typeCompte, reason));
    }

    public void sendPasswordResetEmail(String to, String prenom, String code) {
        send(to, "NEO BTE — Your password reset code",
                EmailTemplateLoader.loadPasswordResetTemplate(prenom, code));
    }

    public void sendFraudeAlertEmail(String to, String adminPrenom, String clientName,
                                     String clientEmail, String alertType,
                                     String severity, String description) {
        send(to, "[FRAUDE ALERT] " + alertType + " — " + clientName,
                EmailTemplateLoader.loadFraudeAlertTemplate(
                        adminPrenom, clientName, clientEmail, alertType, severity, description));
    }

    public void sendPinBypassEmail(String to, String prenom, String code) {
        send(to, "NEO BTE — Code de contournement PIN",
                EmailTemplateLoader.loadPinBypassTemplate(prenom, code));
    }

    public void sendClotureBlockedEmail(String to, String prenom, Long compteId, Double solde) {
        send(to, "NEO BTE — Clôture impossible : solde non nul",
                EmailTemplateLoader.loadClotureBlockedTemplate(prenom, compteId, solde));
    }

    public void sendClotureApprovedEmail(String to, String prenom, Long compteId) {
        send(to, "NEO BTE — Votre compte a été clôturé",
                EmailTemplateLoader.loadClotureApprovedTemplate(prenom, compteId));
    }
}
// APPEND — add this method to the EmailService class body before the closing }