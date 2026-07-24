package com.falcon.booking.common.email.dto;

import java.util.List;

public record EmailRequest(
        String to,
        String subject,
        String body,
        boolean html,
        List<EmailInlineImage> inlineImages,
        List<EmailAttachment> attachments
) {
}