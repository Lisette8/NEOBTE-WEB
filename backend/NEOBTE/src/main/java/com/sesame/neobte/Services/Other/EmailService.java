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

    public void sendInvestmentMaturedEmail(String to, String prenom, String planNom,
                                           double principal, double interet, double total) {
        String html = String.format("""
            <!DOCTYPE html><html><body style="font-family:Arial,sans-serif;background:#f8fafc;padding:32px">
            <div style="max-width:520px;margin:0 auto;background:#fff;border-radius:12px;padding:32px;border:1px solid #e2e8f0">
              <h2 style="color:#0000a0;margin-bottom:4px">Investissement arrivé à échéance</h2>
              <p style="color:#64748b;font-size:14px">Bonjour %s,</p>
              <p style="color:#1e293b">Votre plan <strong>%s</strong> a atteint son échéance.</p>
              <table style="width:100%%;border-collapse:collapse;margin:20px 0">
                <tr style="background:#f1f5f9"><td style="padding:10px 14px;color:#64748b;font-size:13px">Capital investi</td>
                  <td style="padding:10px 14px;text-align:right;font-weight:700">%.3f TND</td></tr>
                <tr><td style="padding:10px 14px;color:#64748b;font-size:13px">Intérêts gagnés</td>
                  <td style="padding:10px 14px;text-align:right;font-weight:700;color:#059669">+%.3f TND</td></tr>
                <tr style="background:#f0fdf4"><td style="padding:12px 14px;color:#065f46;font-weight:700">Total reçu</td>
                  <td style="padding:12px 14px;text-align:right;font-weight:800;font-size:16px;color:#065f46">%.3f TND</td></tr>
              </table>
              <p style="color:#64748b;font-size:13px">Le montant a été crédité sur votre compte NEO BTE.</p>
              <p style="color:#94a3b8;font-size:12px;margin-top:24px">NEO BTE — Banque de Tunisie et des Émirats</p>
            </div></body></html>
            """, prenom, planNom, principal, interet, total);
        send(to, "NEO BTE — Votre investissement a maturé !", html);
    }
}
// APPEND — add this method to the EmailService class body before the closing }