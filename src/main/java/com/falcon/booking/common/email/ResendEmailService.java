package com.falcon.booking.common.email;

import com.falcon.booking.common.email.dto.EmailAttachment;
import com.falcon.booking.common.email.dto.EmailInlineImage;
import com.falcon.booking.common.email.dto.EmailRequest;
import com.falcon.booking.common.email.exception.EmailSendingException;
import com.resend.*;
import com.resend.services.emails.model.Attachment;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service("resendEmailService")
@RequiredArgsConstructor
@Slf4j
public class ResendEmailService implements EmailService {

    private final Resend resend;

    @Value("${mail.from-name}")
    private String from;

    @Override
    public void send(EmailRequest request) {

        try {
            CreateEmailOptions.Builder builder = CreateEmailOptions.builder()
                    .from(from)
                    .to(request.to())
                    .subject(request.subject())
                    .html(request.body());

            if (request.attachments() != null) {

                for (EmailAttachment attachment : request.attachments()) {
                    builder.addAttachment(Attachment.builder()
                                    .fileName(attachment.fileName())
                                    .content(Base64.getEncoder().encodeToString(attachment.data()))
                                    .build());
                }

            }

            if (request.inlineImages() != null) {

                for (EmailInlineImage inline : request.inlineImages()) {
                    builder.addAttachment(Attachment.builder()
                            .fileName(inline.contentId())
                            .content(Base64.getEncoder().encodeToString(inline.data()))
                            .contentType(inline.contentType())
                            .contentId(inline.contentId())
                            .build());
                }

            }

            resend.emails().send(builder.build());

            log.info("Email sent successfully to {}", request.to());

        } catch (Exception ex) {
            log.error("Failed to send email to {}", request.to(), ex);
            throw new EmailSendingException("Unable to send email.", ex);
        }
    }
}