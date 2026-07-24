package com.falcon.booking.common.email.dto;

public record EmailInlineImage(
        String contentId,
        String contentType,
        byte[] data
) { }
