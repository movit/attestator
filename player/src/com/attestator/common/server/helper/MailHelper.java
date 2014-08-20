package com.attestator.common.server.helper;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

public class MailHelper {
    private static final Logger logger = Logger.getLogger(MailHelper.class);

    public static final String SMTP_SERVER = "smtp.yandex.ru";
    public static final String SMTP_USER = "support@examator.ru";
    public static final String SMTP_PASSWORD = "wdLKop90";

    public static void sendMail(String from, String to, String subject,
            String body) {
        
        final String username = SMTP_USER;// change accordingly
        final String password = SMTP_PASSWORD;// change accordingly
        
        // Assuming you are sending email through relay.jangosmtp.net
        String host = SMTP_SERVER;
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.enable", "true");        
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "465");

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);
            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));
            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            // Set Subject: header field
            message.setSubject(subject);
            // Now set the actual message
            message.setText(body);
            // Send message
            Transport.send(message);
        } catch (MessagingException e) {
            logger.error("Unable to send email", e);
        }
    }
}
