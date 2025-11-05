package com.weekfitter.weekfitter_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


/**
 * Služba pro odesílání e-mailů uživatelům.
 *
 * Využívá rozhraní {@link JavaMailSender} poskytované Spring Bootem.
 * Slouží jak pro odesílání notifikací, tak i pro proces obnovy hesla.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    
    public void sendPasswordResetEmail(String to, String token) {
        String resetLink = "http://localhost:3000/reset-password/" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Obnovení hesla - WeekFitter");
        message.setText("Klikněte na následující odkaz pro obnovení hesla:\n" + resetLink);
        mailSender.send(message);
    }

    public void sendNotificationEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

}
