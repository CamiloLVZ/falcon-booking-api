package com.falcon.booking.common.email;

import com.falcon.booking.common.email.dto.EmailAttachment;
import com.falcon.booking.common.email.dto.EmailInlineImage;
import com.falcon.booking.common.email.dto.EmailRequest;
import com.falcon.booking.common.email.exception.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private SmtpEmailService smtpEmailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(smtpEmailService, "from", "falcon@example.com");
        ReflectionTestUtils.setField(smtpEmailService, "fromName", "Falcon Airlines");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @DisplayName("Should send a plain HTML email without attachments or inline images")
    @Test
    void shouldSendPlainHtmlEmail() {
        EmailRequest request = new EmailRequest("passenger@test.com", "Test Subject", "<h1>Hello</h1>", true, null, null);

        smtpEmailService.send(request);

        verify(mailSender).send(mimeMessage);
    }

    @DisplayName("Should send email with an attachment")
    @Test
    void shouldSendEmailWithAttachment() {
        EmailAttachment attachment = new EmailAttachment("boarding-pass.pdf", "application/pdf", new byte[]{1, 2, 3});
        EmailRequest request = new EmailRequest("passenger@test.com", "Boarding Pass", "<p>See attached</p>", true, null, List.of(attachment));

        smtpEmailService.send(request);

        verify(mailSender).send(mimeMessage);
    }

    @DisplayName("Should send email with an inline image")
    @Test
    void shouldSendEmailWithInlineImage() {
        EmailInlineImage logo = new EmailInlineImage("falcon-logo", "image/jpeg", new byte[]{7, 8, 9});
        EmailRequest request = new EmailRequest("passenger@test.com", "Boarding Pass", "<img src='cid:falcon-logo'/>", true, List.of(logo), null);

        smtpEmailService.send(request);

        verify(mailSender).send(mimeMessage);
    }

    @DisplayName("Should throw EmailSendingException when JavaMailSender throws MessagingException")
    @Test
    void shouldThrowEmailSendingException_WhenMessagingExceptionOccurs() throws Exception {

        doThrow(new org.springframework.mail.MailSendException("SMTP failure")).when(mailSender).send(any(MimeMessage.class));

        EmailRequest request = new EmailRequest("passenger@test.com", "Subject", "<p>body</p>", true, null, null);

        assertThatThrownBy(() -> smtpEmailService.send(request))
                .isInstanceOf(EmailSendingException.class);
    }
}
