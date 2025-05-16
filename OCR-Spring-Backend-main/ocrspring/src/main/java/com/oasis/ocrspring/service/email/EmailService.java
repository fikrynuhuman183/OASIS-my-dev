package com.oasis.ocrspring.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class EmailService {
    @Value("${senders.email}")
    private String sendersEmail;
    @Value("${senders.pass}")
    private String sendersPass;
    private final TemplateEngine templateEngine;

    @Autowired
    public EmailService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void sendEmail(String receiversEmail, String type, String message, String name) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sendersEmail, sendersPass);
            }
        });


            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("OCR Tech Team <" + sendersEmail + ">", false));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiversEmail));
            msg.setSubject("OCRP Account Registrations");
            msg.setContent(body(type, message, name), "text/html");
            msg.setSentDate(new java.util.Date());

            Transport.send(msg);

    }

    private String body(String type, String message, String name) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("message", message);
        switch(type) {
            case "ACCEPT":
                return templateEngine.process("emails/accept", context);


            case "REJECT":
                return templateEngine.process("emails/reject", context);
            default:
                return String.valueOf(' ');
        }
    }
}