package com.hyprbank.online.bancavirtual.hyprbank.services.impl;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.hyprbank.online.bancavirtual.hyprbank.services.models.EmailDTO;
import com.hyprbank.online.bancavirtual.hyprbank.services.models.IEmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements IEmailService {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public EmailServiceImpl(JavaMailSender javaMailSender, TemplateEngine templateEngine){
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendMail(EmailDTO email) throws MessagingException{
        try {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = 
                new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(email.getDestinatario());
        helper.setSubject(email.getAsunto());
        
        Context context = new Context();
        context.setVariable("message", email.getMensaje());
        String contentHTML = templateEngine.process("email.html", context);

        helper.setText(contentHTML, true);
        javaMailSender.send(message);
        } catch (Exception e){
            throw new RuntimeException("Error al enviar el correo: "+ e.getMessage(), e);
        }
    }
}