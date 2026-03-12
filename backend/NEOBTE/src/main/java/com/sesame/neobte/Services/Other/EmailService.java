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


    //support
    public void sendSupportResponseEmail(String to, String subject, String adminReply) {

        try {

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("Support reply: " + subject);
            helper.setFrom("neobte-reply@gmail.com");

            String htmlContent = EmailTemplateLoader.loadSupportReplyTemplate(subject, adminReply);

            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //demandeCompte
    public void sendDemandeConfirmationEmail(String to, String prenom, String typeCompte) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("NEO BTE — Your account request has been received");
            helper.setFrom("neobte-reply@gmail.com");

            String html = EmailTemplateLoader.loadDemandeConfirmationTemplate(prenom, typeCompte);
            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendDemandeApprovalEmail(String to, String prenom, String typeCompte, Long compteId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("NEO BTE — Your account has been approved!");
            helper.setFrom("neobte-reply@gmail.com");

            String html = EmailTemplateLoader.loadDemandeApprovalTemplate(prenom, typeCompte, compteId);
            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendDemandeRejectionEmail(String to, String prenom, String typeCompte, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("NEO BTE — Update on your account request");
            helper.setFrom("neobte-reply@gmail.com");

            String html = EmailTemplateLoader.loadDemandeRejectionTemplate(prenom, typeCompte, reason);
            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
