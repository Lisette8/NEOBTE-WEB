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
}
