package com.sesame.neobte.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EmailTemplateLoader {

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
    }
}