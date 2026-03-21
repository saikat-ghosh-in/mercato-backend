package com.mercato.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${mercato.email.from}")
    private String fromEmail;

    @Value("${frontend.base.url}")
    private String frontendUrl;

    @Async
    @Override
    public void sendVerificationEmail(String to, String username, String token) {
        try {
            String verificationLink = frontendUrl + "/verify-email?token=" + token;

            String subject = "Verify Your Email - Mercato";
            String body = buildVerificationEmailBody(username, verificationLink);

            sendHtmlEmail(to, subject, body);

            log.info("Verification email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", to, e);
        }
    }

    @Async
    @Override
    public void sendWelcomeEmail(String to, String username) {
        try {
            String subject = "Welcome to Mercato!";
            String body = buildWelcomeEmailBody(username);

            sendHtmlEmail(to, subject, body);

            log.info("Welcome email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
    }

    @Async
    @Override
    public void sendReactivationConfirmationEmail(String to, String username) {
        try {
            String subject = "Account Reactivated - Mercato";
            String body = buildReactivationConfirmationEmailBody(username);

            sendHtmlEmail(to, subject, body);

            log.info("Reactivation confirmation email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send reactivation confirmation email to: {}", to, e);
        }
    }


    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    private String buildVerificationEmailBody(String username, String verificationLink) {
        return """
                 <!DOCTYPE html>
                 <html>
                 <head>
                     <style>
                         body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                         .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                         .header { background-color: #111827; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                         .content { padding: 30px; background-color: #f9fafb; border-radius: 0 0 8px 8px; }
                         .button { display: inline-block; padding: 12px 24px; background-color: #111827;
                                   color: white; text-decoration: none; border-radius: 8px; margin: 20px 0; }
                         .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 12px; }
                     </style>
                 </head>
                 <body>
                     <div class="container">
                         <div class="header">
                             <h1>Mercato</h1>
                         </div>
                         <div class="content">
                             <h2>Hi %s,</h2>
                             <p>Thanks for signing up! Please verify your email address to complete your registration.</p>
                             <p>Click the button below to verify your email:</p>
                             <a href="%s" class="button">Verify Email</a>
                             <p>Or copy and paste this link into your browser:</p>
                             <p style="word-break: break-all; color: #6b7280; font-size: 12px;">%s</p>
                             <p><strong>This link will expire in 24 hours.</strong></p>
                             <p>If you didn't create an account, you can safely ignore this email.</p>
                         </div>
                         <div class="footer">
                             <p>&copy; 2026 Mercato. All rights reserved.</p>
                         </div>
                     </div>
                 </body>
                 </html>
                """.formatted(username, verificationLink, verificationLink);
    }

    private String buildWelcomeEmailBody(String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #111827; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { padding: 30px; background-color: #f9fafb; border-radius: 0 0 8px 8px; }
                        .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Welcome to Mercato! 🎉</h1>
                        </div>
                        <div class="content">
                            <h2>Hi %s,</h2>
                            <p>Your email has been verified successfully!</p>
                            <p>You can now enjoy all features of Mercato:</p>
                            <ul>
                                <li>Shop from thousands of products</li>
                                <li>Track your orders</li>
                                <li>Save your favorite items</li>
                                <li>Become a seller and start selling</li>
                            </ul>
                            <p>Happy shopping!</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 Mercato. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(username);
    }

    private String buildReactivationConfirmationEmailBody(String username) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #111827; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { padding: 30px; background-color: #f9fafb; border-radius: 0 0 8px 8px; }
                        .success-icon { font-size: 48px; text-align: center; margin: 20px 0; }
                        .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Mercato</h1>
                        </div>
                        <div class="content">
                            <div class="success-icon">✅</div>
                            <h2>Welcome Back, %s!</h2>
                            <p>Your account has been successfully reactivated.</p>
                            <p>You now have full access to all Mercato features:</p>
                            <ul>
                                <li>Browse and shop from thousands of products</li>
                                <li>Track your orders</li>
                                <li>Manage your account</li>
                                <li>Sell your own products</li>
                            </ul>
                            <p>We're glad to have you back!</p>
                            <p>If you didn't request this reactivation, please contact our support team immediately.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 Mercato. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(username);
    }
}
