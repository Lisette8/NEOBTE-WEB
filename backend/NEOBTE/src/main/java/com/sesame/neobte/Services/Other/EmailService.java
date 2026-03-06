package com.sesame.neobte.Services.Utils;

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

            String htmlContent = EmailTemplateLoader
                    .loadSupportReplyTemplate(subject, adminReply);

            helper.setTo(to);
            helper.setSubject("Response to your support ticket");
            helper.setText(htmlContent, true); // true = HTML email

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
