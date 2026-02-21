package com.example.authservice.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${sendgrid.api-key}")
    private String apiKey;

    @Value("${sendgrid.from}")
    private String fromEmail;

    // 🔥 TU TEMPLATE ID
    private static final String TEMPLATE_ID = "d-78171ba20bcc43c2bbba792c25e95d52";

    public void sendVerificationEmail(String to, String verificationCode, String verificationLink) {
        try {

            Email from = new Email(fromEmail);
            Email recipient = new Email(to);

            Mail mail = new Mail();
            mail.setFrom(from);
            mail.setTemplateId(TEMPLATE_ID);

            Personalization personalization = new Personalization();
            personalization.addTo(recipient);

            // 🔥 VARIABLES QUE USA EL TEMPLATE
            personalization.addDynamicTemplateData("verification_code", verificationCode);
            personalization.addDynamicTemplateData("verification_link", verificationLink);

            mail.addPersonalization(personalization);

            SendGrid sg = new SendGrid(apiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            sg.api(request);

        } catch (Exception e) {
            throw new RuntimeException("Error sending verification email", e);
        }
    }
}