package com.govscheme.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String frontendUrl;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.frontend.url:http://localhost:5173}") String frontendUrl) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
    }

    public void sendEmailVerification(String to, String name, String verificationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;

            helper.setTo(to);
            helper.setSubject("Verify your email - Government Scheme Assistant");
            helper.setText(buildVerificationEmail(name, verificationUrl), true);

            mailSender.send(message);
            log.info("EMAIL_VERIFICATION_SENT to={}", to);
        } catch (MessagingException e) {
            // Do not fail registration when mail is unconfigured (demo mode).
            log.warn("EMAIL_VERIFICATION_FAILED to={} reason={}", to, e.getMessage());
        } catch (Exception e) {
            log.warn("EMAIL_VERIFICATION_FAILED to={} reason={}", to, e.getMessage());
        }
    }

    public void sendPasswordReset(String to, String name, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

            helper.setTo(to);
            helper.setSubject("Reset your password - Government Scheme Assistant");
            helper.setText(buildPasswordResetEmail(name, resetUrl), true);

            mailSender.send(message);
            log.info("PASSWORD_RESET_SENT to={}", to);
        } catch (MessagingException e) {
            log.warn("PASSWORD_RESET_FAILED to={} reason={}", to, e.getMessage());
        } catch (Exception e) {
            log.warn("PASSWORD_RESET_FAILED to={} reason={}", to, e.getMessage());
        }
    }

    private String buildVerificationEmail(String name, String verificationUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #0ea5e9;">Government Scheme Assistant</h2>
                    <p>Hello %s,</p>
                    <p>Thank you for registering with Government Scheme Assistant. Please verify your email address by clicking the button below:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #0ea5e9; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; display: inline-block;">Verify Email</a>
                    </div>
                    <p>Or copy this link to your browser:</p>
                    <p style="word-break: break-all; color: #0ea5e9;">%s</p>
                    <p>This link will expire in 24 hours.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 12px; color: #888;">If you didn't create an account, you can safely ignore this email.</p>
                </div>
            </body>
            </html>
            """, name, verificationUrl, verificationUrl);
    }

    private String buildPasswordResetEmail(String name, String resetUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #0ea5e9;">Government Scheme Assistant</h2>
                    <p>Hello %s,</p>
                    <p>You requested a password reset. Click the button below to reset your password:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #0ea5e9; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; display: inline-block;">Reset Password</a>
                    </div>
                    <p>Or copy this link to your browser:</p>
                    <p style="word-break: break-all; color: #0ea5e9;">%s</p>
                    <p>This link will expire in 1 hour.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 12px; color: #888;">If you didn't request a password reset, you can safely ignore this email.</p>
                </div>
            </body>
            </html>
            """, name, resetUrl, resetUrl);
    }
}
