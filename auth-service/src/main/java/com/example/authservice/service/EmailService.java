package com.example.authservice.service;

import com.sendgrid.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api-key}")
    private String apiKey;

    @Value("${sendgrid.from}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) throws IOException {

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
    }

    public void sendVerificationEmail(String email, String verificationCode, String verificationLink) throws IOException {
        String body = "Your verification code is: " + verificationCode +
                "\nClick to verify your email: " + verificationLink;

        sendEmail(email, "Verify your email for Digital Money House", body);
    }
}