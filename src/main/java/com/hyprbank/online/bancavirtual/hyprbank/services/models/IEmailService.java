package com.hyprbank.online.bancavirtual.hyprbank.services.models;

import jakarta.mail.MessagingException;

public interface IEmailService {
    public void sendMail(EmailDTO email) throws MessagingException;
}
