package com.example.authservice.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Content;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${sendgrid.api-key}")
    private String apiKey;

    @Value("${sendgrid.from}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {
        try {
            Email from = new Email(fromEmail);
            Email recipient = new Email(to);
            Content content = new Content("text/plain", body);

            Mail mail = new Mail(from, subject, recipient, content);

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            sg.api(request);

        } catch (Exception e) {
            throw new RuntimeException("Error sending email", e);
        }
    }

    public void sendVerificationEmail(String email, String verificationCode, String verificationLink) {
        String body = "Your verification code is: " + verificationCode +
                "\nClick to verify your email: " + verificationLink;

        sendEmail(email, "Verify your email for Digital Money House", body);
    }
}