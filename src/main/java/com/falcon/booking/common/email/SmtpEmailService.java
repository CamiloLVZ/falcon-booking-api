package com.falcon.booking.common.email;

import com.falcon.booking.common.email.dto.EmailAttachment;
import com.falcon.booking.common.email.dto.EmailInlineImage;
import com.falcon.booking.common.email.dto.EmailRequest;
import com.falcon.booking.common.email.exception.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class SmtpEmailService implements EmailService {

    @Value("${spring.mail.username}")
    private String from;

    @Value("${mail.from-name:Falcon Airlines}")
    private String fromName;

    private final JavaMailSender mailSender;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(EmailRequest request) {
        log.info("Preparing to send email to {} with subject: {}", request.to(), request.subject());
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

            helper.setFrom(new InternetAddress(from, fromName, StandardCharsets.UTF_8.name()));
            helper.setTo(request.to());
            helper.setSubject(request.subject());
            helper.setText(request.body(), request.html());

            if (request.inlineImages() != null) {
                for (EmailInlineImage inline : request.inlineImages()) {
                    helper.addInline(inline.contentId(), new ByteArrayResource(inline.data()), inline.contentType());
                }
            }

            if (request.attachments() != null) {
                log.debug("Adding {} attachment(s) to email", request.attachments().size());
                for (EmailAttachment attachment : request.attachments()) {
                    helper.addAttachment(attachment.fileName(), new ByteArrayResource(attachment.data()), attachment.contentType());
                }
            }

            mailSender.send(mimeMessage);
            log.info("Email successfully sent to {}", request.to());

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to {} due to MessagingException: {}", request.to(), e.getMessage(), e);
            throw new EmailSendingException("Unable to send email.", e);
        }
    }
}