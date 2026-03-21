package com.mercato.Service;

public interface EmailService {

    void sendVerificationEmail(String to, String username, String token);

    void sendWelcomeEmail(String to, String username);

    void sendReactivationConfirmationEmail(String to, String username);
}
