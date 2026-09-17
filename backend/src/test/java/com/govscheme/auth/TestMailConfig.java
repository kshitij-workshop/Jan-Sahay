package com.govscheme.auth;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Replaces the real SMTP sender in tests with an in-memory fake so
 * registration flows stay fast, deterministic and offline.
 */
@TestConfiguration
public class TestMailConfig {

    @Bean
    @Primary
    public FakeJavaMailSender javaMailSender() {
        return new FakeJavaMailSender();
    }

    public static class FakeJavaMailSender implements JavaMailSender {

        private final List<MimeMessage> sent = new CopyOnWriteArrayList<>();

        @Override
        public MimeMessage createMimeMessage() {
            return new MimeMessage(Session.getInstance(new Properties()));
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) throws org.springframework.mail.MailParseException {
            try {
                return new MimeMessage(Session.getInstance(new Properties()), contentStream);
            } catch (Exception e) {
                throw new org.springframework.mail.MailParseException(e);
            }
        }

        @Override
        public void send(MimeMessage mimeMessage) throws MailException {
            sent.add(mimeMessage);
        }

        @Override
        public void send(MimeMessage... mimeMessages) throws MailException {
            sent.addAll(List.of(mimeMessages));
        }

        @Override
        public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
            try {
                MimeMessage message = createMimeMessage();
                mimeMessagePreparator.prepare(message);
                sent.add(message);
            } catch (Exception e) {
                throw new org.springframework.mail.MailSendException("fake send failed", e);
            }
        }

        @Override
        public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
            for (MimeMessagePreparator preparator : mimeMessagePreparators) {
                send(preparator);
            }
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) throws MailException {
        }

        public List<MimeMessage> getSent() {
            return sent;
        }

        public void clear() {
            sent.clear();
        }

        public boolean sentTo(String address) {
            return sent.stream().anyMatch(m -> {
                try {
                    return m.getAllRecipients() != null && java.util.Arrays.stream(m.getAllRecipients())
                        .anyMatch(r -> r.toString().contains(address));
                } catch (Exception e) {
                    return false;
                }
            });
        }
    }
}
